import java.lang.Math;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;
import java.util.random.RandomGenerator;

public class Operations {

    public static double expMaclaurin(double x){
        int n = 10;
        int i = 0;
        int k = 1;
        double f = 1;
        do{
            i++;
            k = i * k;
            f += ((double)1 / k) * Math.pow(x, i);

        }while(i < n);

        return f;
    }
    public static double distance(double[] p1, double[] p2){
        return distance(p1[0], p1[1], p2[0], p2[1]);
    }
    public static double distance(double x1, double y1, double x2, double y2){
        return Math.sqrt(
              Math.pow(x1-x2, 2) 
            + Math.pow(y1-y2, 2)
        );
    }
    public static double totalDistance(int[] path, ArrayList<double[]> ListOfCartesianPoints){
        double totalDistance = 0;
        double x1,x2,y1,y2;
        int length = path.length;
        int i = 0;
        int p = path[i];
        // System.out.print("Operations.totalDistance: ");
        while(i < length-1){
            x1 = ListOfCartesianPoints.get(p)[0];
            y1 = ListOfCartesianPoints.get(p)[1];
            i++;
            p = path[i];
            x2 = ListOfCartesianPoints.get(p)[0];
            y2 = ListOfCartesianPoints.get(p)[1];
            totalDistance += distance(x1, y1, x2, y2);
            // System.out.printf("distance[%d] : %.2f, ", i-1, distance(x1, y1, x2, y2));
        }

        // Loop back
        x1 = ListOfCartesianPoints.get(path[i])[0];
        y1 = ListOfCartesianPoints.get(path[i])[1];
        p = path[0];
        x2 = ListOfCartesianPoints.get(path[0])[0];
        y2 = ListOfCartesianPoints.get(path[0])[1];

        totalDistance += distance(x1, y1, x2, y2);
        // System.out.printf("distance[%d] : %.2f\n", i, distance(x1, y1, x2, y2));

        return totalDistance;
    }

    public static int[] swapEdges(int[] edges2Swap, int[] solution, ArrayList<double[]> nodes) {
        int length = solution.length;
        int[] nextSolution = new int[length];
        int n1 = edges2Swap[0];
        int n2 = edges2Swap[1];
        int i = n1;
        int j = 0;
        int pos;
        do{
            i = (i+1) % length;
            
            if(j > n2)
                pos = length + ((n2-j)%length);
            else
                pos = n2-j;
            
            nextSolution[i] = solution[pos];
            // System.out.printf("2) NextSolution[%d] = %d, Solution[%d] = %d\n", i, nextSolution[i], pos, solution[pos]);

            j = (j+1) % length;

        }while(pos != (n1+1) % length);

        do{
            i = (i+1) % length;
            nextSolution[i] = solution[i];
            // System.out.printf("3) NextSolution[%d] = %d, Solution[%d] = %d\n", i, nextSolution[i], i, solution[i]);
        }while(i != n1);

        return nextSolution;
    }

    public static int[] getPointsToSwap(RandomGenerator rnd, Path path) {
        int n = path.getNodes().size();
        int p1 = rnd.nextInt(n);
        int p2 = rnd.nextInt(n);

        while(p1 == p2 || path.isTabu(p1, p2) || path.isTabu(p2, p1)){
            p2 = rnd.nextInt(n);
        }

        return new int[]{p1,p2};
    }
    public static int[] getEdgesToSwap(RandomGenerator rnd, Path path) {
        int n = path.getNodes().size();
        int p1 = rnd.nextInt(n);
        int p2 = rnd.nextInt(n);

        while(p1 == p2 || p1 == path.increaseMod(p2) || p1 == path.decreaseMod(p2) 
            || path.isTabu(p1, p2) || path.isTabu(p2, p1)){

            p2 = rnd.nextInt(n);
        }

        return new int[]{p1,p2};    
    }

    public static LinkedList<int[]> getKEdges(Path path, int k){
        RandomGenerator rnd = new Random(System.nanoTime());
        LinkedList<int[]> edges = new LinkedList<int[]>();
        LinkedList<Integer> taken = new LinkedList<>();

        int u,v; 
        int[] e;
        while(k > 0){
            do{
                u = rnd.nextInt(path.getNodes().size());
                v = path.increaseMod(u);
                e = new int[]{u,v};
            }while(taken.contains(u) || taken.contains(v));
            taken.push(u);
            taken.push(v);
            edges.push(e);
            k--;
        }

        return edges;
    }
    public static LinkedList<int[]> getKBestPair(LinkedList<int[]> edges, Path path){

        LinkedList<int[]> pairs = new LinkedList<>();
        LinkedList<Integer> taken = new LinkedList<>();
        while(!edges.isEmpty()){
            int[] n = edges.pop();
            
            // get best neighbour logic
            double[] p1 = path.getNodes().get(path.getPath()[n[0]]);
            double[] p1_1 = path.getNodes().get(path.getPath()[n[1]]);
            double distance1 = path.getDistance()[n[0]];
    
            int stop = path.decreaseMod(n[0]);
            while(taken.contains(stop)){
                stop = path.decreaseMod(stop);
            }
            int i = path.increaseMod(n[1]);

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

                do{
                    i = path.increaseMod(i);
                }while(taken.contains(i));
            }
            taken.push(n[1]);
            pairs.push(n);
        }

        return pairs;

    }

    public static Path getKOpt(Path path, int k){
        Path p = new Path(path);
        // System.out.println("Get K Opt. W/ k: " + k);
        // System.out.println("Old Solution is");
        // for (int is : p.getPath()) {
        //     System.out.printf("%d ", is);
        // }
        LinkedList<int[]> e = getKEdges(p, k);
        // System.out.println("\nGot follwing random edges");
        // for (int[] is : e) {
        //     System.out.printf("(%d,%d) ", is[0], is[1]);
        // }
        // System.out.println("\nGot follwing best new edges");
        e = getKBestPair(e, p);
        // for (int[] is : e) {
        //     System.out.printf("(%d,%d) ", is[0], is[1]);
        // }
        // System.out.println("\n emptying list");
        while(!e.isEmpty()){
            int[] n = e.pop();
            // System.out.println("swapEdges");
            int[] nextSolution = swapEdges(n, p.getPath(), p.getNodes());
            // System.out.println("update");
            p.update(n[0], n[1], nextSolution);
        }
        // System.out.println("New Solution is");
        // for (int is : p.getPath()) {
        //     System.out.printf("%d ", is);
        // }
        // System.out.println("");
        return p;
    }

}
