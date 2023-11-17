import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.random.RandomGenerator;

public class TravelingSalesman {
    private ExecutorService executor 
        = Executors.newFixedThreadPool(20);
    /**
     *   PSEUDOCODE FROM COURSERA. 
     *   BASIC LOCAL SEARCH.
     *   function LocalSearch(f, N, L, S) { 
     *     s := generateInitialSolution(); 
     *     s? := s;
     * 
     *     for k := 1 to MaxTrials do
     *       if satisfiable(s) AND f(s) < f(s?) then 
     *         s? := s;
     *       s := S(L(N(s),s),s); 
     * 
     *     return s?;
     *   }
     * 
     *   METROPOLITAN HEURESTICS.
     *   function S-Metropolis[t](N,s) {
     *     select n from N with probability 1/#N; 
     *     if f(n) <= f(s) then
     *       return n;
     *     else with probability exp( -(f(n)-f(s)) / temperature)
     *       return n;
     *     else
     *       return s;
     *   }
     * 
     *   SIMULATED ANNEALING.
     *   function SimulatedAnnealing(f,N) { 
     *     s := generateInitialSolution();
     *     t1 := initTemperature(s);
     *     s?:=s;
     * 
     *     for k := 1 to MaxSearches do
     *       s := LocalSearch(f,N,L-All,S-Metropolis[tk],s); 
     *       if f(s) < f(s?) then
     *           s? := s;
     *       tk+1 := updateTemperature(s,tk);
     *     
     *     return s?;
     *   }
     */

    public static Path solve(int nodeCount, ArrayList<double[]> nodes){
        int[] solution = threadedBestOfRndSolution(nodes, nodeCount);
        Path path = new Path(solution, nodes);
        long time = System.nanoTime();
        long breakTime = 1 * 60 * 1000000000L;
        // System.out.println("Node Count: " + nodeCount);

        
        // System.out.println("Total distance before random search: " + path.getTotalDistance());
        // while(System.nanoTime()-time < 5*breakTime && nodeCount > 1000){
        //     Path path2 = threadedLocalSearch(nodes, nodeCount);
        //     if(path2.getTotalDistance() < path.getTotalDistance())
        //     path = path2;
        // }
        // System.out.println("Total distance after random search: " + path.getTotalDistance());
        
        if(nodeCount < 100)
            breakTime  = 5 * breakTime;
        else if(nodeCount < 600)
            breakTime  = 7 * breakTime;
        else
            breakTime  = 15 * breakTime;

        // breakTime *= 2;
        time = System.nanoTime();
        int k = 1024;
        // boolean progress = false;
        double bestDistance = path.getTotalDistance();
        double prevDistance = bestDistance;
        Path bestPath = path;
        while(System.nanoTime()-time < breakTime){
            // System.out.println("[" + (System.nanoTime()-time)/1000000000L + "/" + breakTime/1000000000L +" s] Starting simulated annealing w/ k: " + k);
            prevDistance = path.getTotalDistance();
            path = simulatedAnnealing(path, k);
            // System.out.println("Total distance from annealing: " + path.getTotalDistance() + "\t prevDistance: " + prevDistance);
            if(path.getTotalDistance() != prevDistance){
                if(path.getTotalDistance() < bestDistance){
                    bestDistance = path.getTotalDistance();
                    bestPath = path;
                }
                Path pathLS = new Path(path);
                pathLS = localSearch(pathLS);
                // System.out.println("Total distance from LS : " + pathLS.getTotalDistance());
                if(pathLS.getTotalDistance() < bestDistance){
                    bestDistance = pathLS.getTotalDistance();
                    bestPath = pathLS;
                    // path = pathLS;
                }
            }
            else{
                solution = threadedBestOfRndSolution(nodes, nodeCount);
                path = new Path(solution, nodes);
                // path = bestPath;
                k = k + 128;
            }

        }
        // System.out.println("[" + (System.nanoTime()-time)/1000000000L + "/" + breakTime/1000000000L +" s] Stopped");

        return bestPath;
    }

    public static Path solvePermutation(int nodeCount, ArrayList<double[]> nodes){
        int[] solution = getRandomSolution(nodeCount);
        Path path = new Path(solution, nodes);
        long time = System.nanoTime();
        long breakTime = 5 * 60 * 1000000000L;
        // if(nodeCount < 10000)
        //     breakTime = 5 * 60 * 1000000000L;

        int k = 256;
        while(System.nanoTime()-time < breakTime){
            path = simulatedAnnealingPermutation(path, k);
            path = localSearch(path);
            k = k << 1;
        }

        return path;
    }

    public static Path simulatedAnnealingPermutation(Path path, int k){
        double totalDistance = path.getTotalDistance();
        double bestDistance = totalDistance;
        Path bestPath = path;

        long iterations = 0;

        int temperature = coolingSchedule(iterations, k);
        while(temperature > 1){
            iterations++;
            path = metropolisPermutation(path, temperature);
            totalDistance = path.getTotalDistance();

            if(totalDistance < bestDistance){
                bestDistance = totalDistance;
                bestPath = path;
            }
            
            temperature = coolingSchedule(iterations, k);

        }

        return bestPath;
    }
    
    public static Path simulatedAnnealing(Path path, int k){
        double totalDistance = path.getTotalDistance();
        double bestDistance = totalDistance;
        Path bestPath = path;

        long iterations = 0;
        int noProgress = 0;
        long reposition = (path.getNodes().size());

        int temperature = coolingSchedule(iterations, k);
        while(temperature > 2){
            iterations++;
            // if(iterations % 100 == 0)
                // System.out.println("Calling metro w/ K OPT at iteration \t" + iterations
                // + "\t temp: " + temperature + "\t best distance: " + bestDistance
                // + "\t with noProgress [" + noProgress + "/" + reposition + "]");
            path = metropolisKOpt(path, temperature, 2);
            Path path3 = metropolisKOpt(path, temperature, 3);
            Path path4 = metropolisKOpt(path, temperature, 4);
            Path path5 = metropolisKOpt(path, temperature, 5);
            Path path6 = metropolisKOpt(path, temperature, 6);
            if(path3.getTotalDistance() < path.getTotalDistance()){
                path = path3;
            }
            if(path4.getTotalDistance() < path.getTotalDistance()){
                path = path4;
            }
            if(path5.getTotalDistance() < path.getTotalDistance()){
                path = path5;
            }
            if(path6.getTotalDistance() < path.getTotalDistance()){
                path = path6;
            }

            // path = metropolis(path, temperature);
            totalDistance = path.getTotalDistance();

            if(totalDistance < bestDistance){
                bestDistance = path.getTotalDistance();
                bestPath = path;
                noProgress = 0;
            }else{
                noProgress++;
            }

            if(noProgress > reposition){
                // System.out.println("Restart/break");
                // System.out.println("Reposition w/ random solution");
                // path = new Path(threadedBestOfRndSolution(path.getNodes(), path.getNodes().size()), path.getNodes());
                // path = bestPath;
                // noProgress = 0;
                break;
            }
            
            temperature = coolingSchedule(iterations, k);

        }

        return bestPath;
    }
    
    public static int[] simulatedAnnealing(int nodeCount, ArrayList<double[]> nodes){
        int[] solution = getRandomSolution(nodeCount);
        solution = localSearch(nodeCount, solution, nodes);
        double totalDistance = Operations.totalDistance(solution, nodes);
        int[] bestSolution = solution;
        double bestDistance = totalDistance;

        int k = 200;
        long iterations = 0;

        int noProgress = 0;
        long reposition = k*nodeCount;

        long time = System.nanoTime();
        long breakTime = 1 * 60 * 1000000000L;

        int temperature = coolingSchedule(iterations, k);
        while(temperature > 1){
            iterations++;
            solution = metropolis(nodeCount, solution, totalDistance, nodes, temperature);
            totalDistance = Operations.totalDistance(solution, nodes);

            if(totalDistance < bestDistance){
                solution = localSearch(nodeCount, solution, nodes);
                totalDistance = Operations.totalDistance(solution, nodes);
                bestDistance = totalDistance;
                bestSolution = solution;
                noProgress = 0;
            }else{
                noProgress++;
            }

            if(noProgress > reposition){
                System.out.println("Reposition!");
                solution = getRandomSolution(nodeCount);
                noProgress = 0;
                if(System.nanoTime()-time > breakTime){
                    System.out.println("Drop k! " + k);
                    k = k >> 1;
                    System.out.println("new k " + k);
                    time = System.nanoTime();
                }
            }

            temperature = coolingSchedule(iterations, k);

        }
        System.out.println("total iterations: " + iterations);


        return bestSolution;
    }

    private static int coolingSchedule(long iterations, int k) {
        long t = 0;
        double c = 1.4;
        long tmp = Math.round(
            Math.exp(c * Math.log(iterations))
            );

        t = k - tmp;

        if(iterations > 100*k){
            while(iterations > 100*k){
                t--;
                iterations -= k;
            }
        }

        return (int)t;
    }

    public static Path metropolisPermutation(Path path, int temperature){
        int points = path.getNodes().size();
        double prevDistance = path.getTotalDistance();
        // int k = 3;
        RandomGenerator rnd = new Random((long) points + System.nanoTime());
        int[] points2Swap = Operations.getPointsToSwap(rnd, path);

        Path nextPath = new Path(path);
        nextPath.swapPoints(points2Swap[0], points2Swap[1]);
        double newDistance = nextPath.getTotalDistance();

        if(newDistance <= prevDistance)
            return nextPath;

        double probability = Math.exp(-(newDistance-prevDistance) / (double)temperature);
        // double probMac = Operations.expMaclaurin(-(newDistance-prevDistance) / (double)temperature);
        // System.out.println("prob: " + probability + " \t with a decrease of " + (newDistance-prevDistance) + " \t at temp " + temperature);
        if(probability > rnd.nextDouble(1)){
            return nextPath;
        }
        else
            return path;

    }
    
    public static Path metropolisKOpt(Path path, int temperature, int k){
        double prevDistance = path.getTotalDistance();
        RandomGenerator rnd = new Random(System.nanoTime());

        Path nextPath = new Path(path);
        nextPath = bestKOpt(nextPath, k);
        // nextPath = Operations.getKOpt(nextPath, k);
        double newDistance = nextPath.getTotalDistance();

        if(newDistance <= prevDistance)
            return nextPath;

        double probability = Math.exp(-(newDistance-prevDistance) / (double)temperature);
        if(probability > rnd.nextDouble(1)){
            return nextPath;
        }
        else
            return path;

    }

    private static Path bestKOpt(Path p, int k){
        TravelingSalesman tsp = new TravelingSalesman();
        Future<Path> future1 = tsp.threadedKOpt(p,k);
        Future<Path> future2 = tsp.threadedKOpt(p,k);
        Future<Path> future3 = tsp.threadedKOpt(p,k);
        Future<Path> future4 = tsp.threadedKOpt(p,k);

        try {
            while(!future1.isDone());
            p = future1.get();
            while(!future2.isDone());
            if(future2.get().getTotalDistance() < p.getTotalDistance())
                p = future2.get();
            while(!future3.isDone());
            if(future3.get().getTotalDistance() < p.getTotalDistance())
                p = future3.get();
            while(!future4.isDone());
            if(future4.get().getTotalDistance() < p.getTotalDistance())
                p = future4.get();
        } catch (InterruptedException e) {
            System.exit(k);
            e.printStackTrace();
        } catch (ExecutionException e) {
            System.exit(k);
            e.printStackTrace();
        }
        
        tsp.executor.shutdown();
        return p;
    }

    private Future<Path> threadedKOpt(Path p, int k){
        return executor.submit(() -> {
            return Operations.getKOpt(p, k);
        });
    }
    
    public static Path metropolis(Path path, int temperature){
        int nodeCount = path.getNodes().size();
        double prevDistance = path.getTotalDistance();
        // int k = 3;
        RandomGenerator rnd = new Random((long) nodeCount + System.nanoTime());
        int[] edges2Swap = Operations.getEdgesToSwap(rnd, path);

        Path nextPath = new Path(path);
        int[] nextSolution = Operations.swapEdges(edges2Swap, nextPath.getPath(), nextPath.getNodes());
        double newDistance = nextPath.update(edges2Swap[0], edges2Swap[1], nextSolution);

        if(newDistance <= prevDistance)
            return nextPath;

        double probability = Math.exp(-(newDistance-prevDistance) / (double)temperature);
        // double probMac = Operations.expMaclaurin(-(newDistance-prevDistance) / (double)temperature);
        // System.out.println("prob: " + probability + " \t with a decrease of " + (newDistance-prevDistance) + " \t at temp " + temperature);
        if(probability > rnd.nextDouble(1)){
            return nextPath;
        }
        else
            return path;

    }
    
    public static int[] metropolis(int nodeCount, int[] solution, double prevDistance, ArrayList<double[]> nodes, int temperature){
        RandomGenerator rnd = new Random((long) nodeCount + System.nanoTime());
        int[] edges2Swap = {rnd.nextInt(nodeCount), rnd.nextInt(nodeCount)};

        while(edges2Swap[1] == edges2Swap[0] || edges2Swap[1] == edges2Swap[0]+1){
            edges2Swap[1] = rnd.nextInt(nodeCount);
        }

        int[] nextSolution = Operations.swapEdges(edges2Swap, solution, nodes);
        double newDistance = Operations.totalDistance(nextSolution, nodes);
        if(newDistance <= prevDistance)
            return nextSolution;

        double probability = Math.exp(-(newDistance-prevDistance) / (double)temperature);
        if(probability > rnd.nextDouble(1)){
            return nextSolution;
        }
        else
            return solution;

    }

    public static Path localSearch(Path path){
        ArrayList<double[]> nodes = path.getNodes();
        double currentMinimum = path.getTotalDistance();
        double totalDistance = currentMinimum;
        Path newPath = new Path(path);

        int next = 0;
        int loop = 0;
        do{
            currentMinimum = totalDistance;
            path = newPath;
            int[] edges2Swap = get2OPT(newPath, next);
            if(edges2Swap[0] != edges2Swap[1]){
                // System.out.println("Swapping " + edges2Swap[0] + " with " + edges2Swap[1]);
                int[] nextSolution = Operations.swapEdges(edges2Swap, newPath.getPath(), nodes);
                totalDistance = newPath.update(edges2Swap[0], edges2Swap[1], nextSolution);
                loop = 0;
            }
            else{
                loop++;
            }
            next++;
            if(next >= nodes.size())
                next = 0;
        }while(totalDistance < currentMinimum || loop >= path.getNodes().size());   

        return path;
    }
    public static int[] localSearch(int nodeCount, int[] solution, ArrayList<double[]> nodes){
        double currentMinimum = Operations.totalDistance(solution, nodes);

        int[] edges2Swap = get2OPT(solution, nodes);

        int[] nextSolution = Operations.swapEdges(edges2Swap, solution, nodes);
        double totalDistance = Operations.totalDistance(nextSolution, nodes);

        while(totalDistance < currentMinimum){
            currentMinimum = totalDistance;
            solution = nextSolution;

            edges2Swap = get2OPT(solution, nodes);        

            nextSolution = Operations.swapEdges(edges2Swap, solution, nodes);
            totalDistance = Operations.totalDistance(nextSolution, nodes);;
        }

        return solution;
    }

    public static int[] get2OPT(Path path, int start){

        // Old edges are (p1,p1+1), (p2,p2+1)
        // new edges are (p1,p2), (p1+1,p2+1)

        int[] n = {start, start};
        
        // get best neighbour logic
        double[] p1 = path.getNodes().get(path.getPath()[start]);
        double[] p1_1 = path.getNodes().get(path.getPath()[path.increaseMod(start)]);
        double distance1 = path.getDistance()[start];

        int stop = path.decreaseMod(n[0]);
        int i = path.increaseMod(n[0]+1);
        
        while(i != stop){
            double[] p2 = path.getNodes().get(path.getPath()[i]);
            double[] p2_1 = path.getNodes().get(path.getPath()[path.increaseMod(i)]);
            double distance2 = path.getDistance()[i];

            double tmpDistance1 = Operations.distance(p1,p2);
            double tmpDistance2 = Operations.distance(p1_1,p2_1);
            if((tmpDistance1 + tmpDistance2) < (distance1 + distance2)){
                distance1 = tmpDistance1;
                n[1] = i;
                // break;
            }
            i = path.increaseMod(i);
        }

        return n;
    }
    
    public static int[] get2OPT(Path path){
        int length = path.getPath().length;

        // 2-Opt
        int[] n = {0, 0};
        double largestDistance = 0;
        // Find current largest distance
        for (int i = 0; i < length; i++){
            if(path.getDistance()[i] > largestDistance){
                n[0] = i;
                largestDistance = path.getDistance()[i];
            }
        }
        // System.out.println("2OPT: Got n[0] = " + n[0]);

        // get best neighbour
        int stop = path.decreaseMod(n[0]);
        int i = path.increaseMod(n[0]+1);
        double[] p1 = path.getNodes().get(path.getPath()[n[0]]);
        double[] p2 = path.getNodes().get(path.getPath()[i]);
        largestDistance = Operations.distance(p1,p2);
        n[1] = i;

        i = path.increaseMod(i);
        while(i != stop){
            // System.out.println("Checking i = " + i);
            p2 = path.getNodes().get(path.getPath()[i]);
            double tmpDistance = Operations.distance(p1,p2);
            if(tmpDistance < largestDistance){
                largestDistance = tmpDistance;
                n[1] = i;
            }
            i = path.increaseMod(i);
        }

        return n;
    }
    
    public static int[] get2OPT(int[] solution, ArrayList<double[]> nodes){
        int length = solution.length;

        // 2-Opt
        int[] n = {0, 0};
        double d = 0;
        // Find current largest 2 distances 
        int i = 0;
        while(i < length - 1){
            double[] p1 = nodes.get(solution[i]);
            i++;
            double[] p2 = nodes.get(solution[i]);

            double temp_d = Operations.distance(p1,p2);
            if(temp_d > d){
                d = temp_d;
                n[0] = i - 1;
            }
        }

        double[] p1 = nodes.get(solution[i]);
        double[] p2 = nodes.get(solution[0]);
        double temp_d = Operations.distance(p1,p2);
        if(temp_d > d){
            d = temp_d;
            n[0] = i;
        }

        // get best neighbour
        p1 = nodes.get(solution[n[0]]);
        for(i = 0; i < n[0]-1; i++){
            p2 = nodes.get(solution[i]);
            temp_d = Operations.distance(p1,p2);
            if(temp_d < d){
                d = temp_d;
                n[1] = i;
            }
        }

        for(i = n[0]+2; i < length; i++){
            p2 = nodes.get(solution[i]);
            temp_d = Operations.distance(p1,p2);
            if(temp_d < d){
                d = temp_d;
                n[1] = i;
            }
        }

        return n;
    }

    private static int[] getInitalSolution(int nodeCount) {
        int[] solution = new int[nodeCount];
        int i = 0;
        while(i < nodeCount)
            solution[i] = i++;

        return solution;
    }


    private static int[] getBestOfRndSolution(ArrayList<double[]> nodes, int nodeCount, int selection){
        int[] solution = getRandomSolution(nodeCount);
        double distance = Operations.totalDistance(solution, nodes);
        while(--selection > 0){
            int[] tmpS = getRandomSolution(nodeCount);
            double tmpD = Operations.totalDistance(tmpS, nodes);
            if(tmpD < distance){
                distance = tmpD;
                solution = tmpS;
            }
        }
        return solution;
    }
    private static Path threadedLocalSearch(ArrayList<double[]> nodes, int nodeCount){
        TravelingSalesman tsp = new TravelingSalesman();
        Path path = null;
        Future<Path> future1 = tsp.threadedRndWithLocalSearch(nodeCount, nodes);
        Future<Path> future2 = tsp.threadedRndWithLocalSearch(nodeCount, nodes);
        Future<Path> future3 = tsp.threadedRndWithLocalSearch(nodeCount, nodes);
        Future<Path> future4 = tsp.threadedRndWithLocalSearch(nodeCount, nodes);
        Future<Path> future5 = tsp.threadedRndWithLocalSearch(nodeCount, nodes);
        Future<Path> future6 = tsp.threadedRndWithLocalSearch(nodeCount, nodes);
        Future<Path> future7 = tsp.threadedRndWithLocalSearch(nodeCount, nodes);
        Future<Path> future8 = tsp.threadedRndWithLocalSearch(nodeCount, nodes);
        Future<Path> future9 = tsp.threadedRndWithLocalSearch(nodeCount, nodes);
        Future<Path> future10 = tsp.threadedRndWithLocalSearch(nodeCount, nodes);
        try {
            while(!future1.isDone());
            path = future1.get();

            while(!future2.isDone());
            if(future2.get().getTotalDistance() < path.getTotalDistance()){
                path = future2.get();
            }
            while(!future3.isDone());
            if(future3.get().getTotalDistance() < path.getTotalDistance()){
                path = future3.get();
            }
            while(!future4.isDone());
            if(future4.get().getTotalDistance() < path.getTotalDistance()){
                path = future4.get();
            }
            while(!future5.isDone());
            if(future5.get().getTotalDistance() < path.getTotalDistance()){
                path = future5.get();
            }
            while(!future6.isDone());
            if(future6.get().getTotalDistance() < path.getTotalDistance()){
                path = future6.get();
            }
            while(!future7.isDone());
            if(future7.get().getTotalDistance() < path.getTotalDistance()){
                path = future7.get();
            }
            while(!future8.isDone());
            if(future8.get().getTotalDistance() < path.getTotalDistance()){
                path = future8.get();
            }
            while(!future9.isDone());
            if(future9.get().getTotalDistance() < path.getTotalDistance()){
                path = future9.get();
            }
            while(!future10.isDone());
            if(future10.get().getTotalDistance() < path.getTotalDistance()){
                path = future10.get();
            }

        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            System.exit(1);
        } catch (ExecutionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            System.exit(1);
        }
        tsp.executor.shutdown();

        return path;
    }
    private static int[] threadedBestOfRndSolution(ArrayList<double[]> nodes, int nodeCount){
        TravelingSalesman tsp = new TravelingSalesman();
        int[] solution = null;
        Future<int[]> future1 = tsp.threadedRndSolution(nodeCount);
        Future<int[]> future2 = tsp.threadedRndSolution(nodeCount);
        Future<int[]> future3 = tsp.threadedRndSolution(nodeCount);
        Future<int[]> future4 = tsp.threadedRndSolution(nodeCount);
        Future<int[]> future5 = tsp.threadedRndSolution(nodeCount);
        Future<int[]> future6 = tsp.threadedRndSolution(nodeCount);
        Future<int[]> future7 = tsp.threadedRndSolution(nodeCount);
        Future<int[]> future8 = tsp.threadedRndSolution(nodeCount);
        Future<int[]> future9 = tsp.threadedRndSolution(nodeCount);
        Future<int[]> future10 = tsp.threadedRndSolution(nodeCount);
        try {
            while(!future1.isDone());
            double distance = Operations.totalDistance(future1.get(), nodes);
            solution = future1.get();

            while(!future2.isDone());
            double distance2 = Operations.totalDistance(future2.get(), nodes);
            if(distance2 < distance){
                distance = distance2;
                solution = future2.get();
            }
            while(!future3.isDone());
            distance2 = Operations.totalDistance(future3.get(), nodes);
            if(distance2 < distance){
                distance = distance2;
                solution = future3.get();
            }
            while(!future4.isDone());
            distance2 = Operations.totalDistance(future4.get(), nodes);
            if(distance2 < distance){
                distance = distance2;
                solution = future4.get();
            }
            while(!future5.isDone());
            distance2 = Operations.totalDistance(future5.get(), nodes);
            if(distance2 < distance){
                distance = distance2;
                solution = future5.get();
            }
            while(!future6.isDone());
            distance2 = Operations.totalDistance(future6.get(), nodes);
            if(distance2 < distance){
                distance = distance2;
                solution = future6.get();
            }
            while(!future7.isDone());
            distance2 = Operations.totalDistance(future7.get(), nodes);
            if(distance2 < distance){
                distance = distance2;
                solution = future7.get();
            }
            while(!future8.isDone());
            distance2 = Operations.totalDistance(future8.get(), nodes);
            if(distance2 < distance){
                distance = distance2;
                solution = future8.get();
            }
            while(!future9.isDone());
            distance2 = Operations.totalDistance(future9.get(), nodes);
            if(distance2 < distance){
                distance = distance2;
                solution = future9.get();
            }
            while(!future10.isDone());
            distance2 = Operations.totalDistance(future10.get(), nodes);
            if(distance2 < distance){
                distance = distance2;
                solution = future10.get();
            }

        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            System.exit(1);
        } catch (ExecutionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            System.exit(1);
        }
        tsp.executor.shutdown();

        return solution;
    }

    private Future<int[]> threadedRndSolution(int nodeCount) {
        
        int[] solution = new int[nodeCount];
        int i = 0;
        while(i < nodeCount)
            solution[i] = i++;

        RandomGenerator rnd = new Random(System.nanoTime());
        while(i-- > 0){
            int a = rnd.nextInt(nodeCount);
            int b = rnd.nextInt(nodeCount);

            int tmp = solution[a];
            solution[a] = solution[b];
            solution[b] = tmp;
        }
        return executor.submit(() -> {
            return solution;
        });
    }

    private Future<Path> threadedRndWithLocalSearch(int nodeCount, ArrayList<double[]> nodes) {
        
        int[] solution = new int[nodeCount];
        int i = 0;
        while(i < nodeCount)
            solution[i] = i++;

        RandomGenerator rnd = new Random(System.nanoTime());
        while(i-- > 0){
            int a = rnd.nextInt(nodeCount);
            int b = rnd.nextInt(nodeCount);

            int tmp = solution[a];
            solution[a] = solution[b];
            solution[b] = tmp;
        }
        Path path = localSearch(new Path(solution, nodes));
        return executor.submit(() -> {
            return path;
        });
    }

    private static int[] getRandomSolution(int nodeCount) {
        int[] solution = new int[nodeCount];
        int i = 0;
        while(i < nodeCount)
            solution[i] = i++;

        RandomGenerator rnd = new Random(System.nanoTime());
        while(i-- > 0){
            int a = rnd.nextInt(nodeCount);
            int b = rnd.nextInt(nodeCount);

            int tmp = solution[a];
            solution[a] = solution[b];
            solution[b] = tmp;
        }

        return solution;
    }

    public static Path tabuSearch(Path path, int iterations){
        // Keep record of recent states that are tabu
        // choose the the next state as the best of legal states

        return path;
    }
}