import java.util.ArrayList;
import java.util.LinkedList;
import java.util.stream.IntStream;

public class ColorGraph {
    int[] orderByGrade;
    ArrayList<LinkedList<Integer>> edgeList;
    ArrayList<LinkedList<Integer>> domain;
    ArrayList<LinkedList<Integer>> solution = null;
    boolean solutionFound = false;
    int minColor;
    int nodeCount;

    public ColorGraph(ArrayList<LinkedList<Integer>> n, int nc){
        this.edgeList = n;
        this.nodeCount = nc;
        this.minColor = nc;
        this.domain = new ArrayList<LinkedList<Integer>>();
        this.orderByGrade = new int[nodeCount];
        sortOrderByGrade();
        int i = 0;
        for(i = 0; i < this.nodeCount; i++){
            this.domain.add(null);
        }
        for(i = 0; i < this.nodeCount; i++){
            this.domain.set(this.orderByGrade[i], 
                new LinkedList<Integer>(
                IntStream
                .range(0, i+1)
                .boxed()
                .toList()));
        }
        // while(i < nc){
        //     this.domain.add(new LinkedList<Integer>(
        //         IntStream
        //         .range(0, this.orderByGrade[i++]+1)
        //         .boxed()
        //         .toList()));
        // }
        this.minColor = decideStartColorTarget(this.nodeCount, this.edgeList);

        // for(i = 0; i < this.nodeCount; i++){
        //     System.out.printf("order[%d] = %d\n", i, this.orderByGrade[i]);
        //     for (int d : edgeList.get(this.orderByGrade[i])) {
        //         System.out.print(d + " ");
        //     }
        //     System.out.println("");
        // }
        // System.out.println("edge list");
        // printArrayList(edgeList);
        // System.out.println("domain list");
        // printArrayList(domain);
    }

    class DomainStore{
        ArrayList<LinkedList<Integer>> domain;
        int colors;
        int color;
        int vertice;

        public DomainStore(ArrayList<LinkedList<Integer>> d, int cls, int v, int c){
            this.domain = new ArrayList<LinkedList<Integer>>(d);
            this.colors = cls;
            this.color = c;
            this.vertice = v;
        }
    }

    private int decideStartColorTarget(int v, ArrayList<LinkedList<Integer>> elist){
        int e = 0;
        // int single_nodes = 0;
        for (LinkedList<Integer> list : elist) {
            e += list.size();
            // if(list.size() == 1)
            //     single_nodes++;
        }
        e = e >> 1;
        // System.out.println("Single nodes: " + single_nodes);
        int maxE = getEdgesCompleteGraph(v);
        float fraction  = e / (float)maxE;

        int target   = Math.round(fraction * v);
        // target       = target >> 1;
        target = target < (v/2) ? target : (v/2);
        // System.out.println("New target: " + target);
        return target;
    }

    private int stepUp(int prevGoal, int vertices){
        int fraction = (vertices / 10);
        int newGoal = prevGoal + fraction > prevGoal ? prevGoal + fraction : prevGoal++;
        return newGoal;
    }

    public int getEdgesCompleteGraph(int vertices){
        int edges = 0;
        for(int i = 1; i < vertices; i++){
            edges += i;
        }

        return edges;
    }
    public ArrayList<LinkedList<Integer>> getNodeList(){
        return this.edgeList;
    }

    public ArrayList<LinkedList<Integer>> getDomainList(){
        return this.domain;
    }
    
    public ArrayList<LinkedList<Integer>> getSolution(){
        return this.solution;
    }
    
    public boolean solver(){
        // System.out.println("Solve with minColor wow: " + this.minColor);
        // 1. set first color
        int v         = this.orderByGrade[0];
        // printArrayList(this.domain);
        // System.out.printf("After setColor, i %d = v %d, c %d\n", 0, v, 0);
        setColor(this.domain, v, 0);
        prune(this.domain, v, 0);
        // printArrayList(this.domain);
        //System.exit(1);
        // 2. create stack for backtracking
        LinkedList<DomainStore> stack = new LinkedList<>();
        stack.push(new DomainStore(domain, minColor, 0, 0));
        return solve(stack);
    }

    boolean solve(LinkedList<DomainStore> stack){
        long startTime = System.nanoTime();
        long abortTimeNanoSeconds = 50000000000L;
        ArrayList<LinkedList<Integer>> localD = null;
        int it            = 0;
        // DomainStore start = stack.pop();
        // localD            = start.domain;
        DomainStore start;
        int c = 0;
        int colors = 0;
        int i = 0;
        boolean pop       = true;

        while(!stack.isEmpty()){
            if((System.nanoTime() - startTime) > abortTimeNanoSeconds)
                return false;

            if(pop){
                start    = stack.pop();
                localD   = start.domain;
                c        = start.color + 1;
                colors   = start.colors;
                i        = start.vertice + 1;
                pop      = false;
                // System.out.print("*********************************POPPOPOPOP*************************");

            }
            // System.out.print("Local Domain:");
            // printArrayList(localD);
            // System.out.print("");
            it++;
            colors++;

            int v = this.orderByGrade[i];
            while(localD.get(v).size() == 1 && i < this.nodeCount){
                if(++i < this.nodeCount)
                    v = this.orderByGrade[i];
            }

            if(i < this.nodeCount){
                ArrayList<LinkedList<Integer>> tmpD 
                    = new ArrayList<LinkedList<Integer>>(localD);                
                do{
                    // System.out.printf("Trying to add color %d to vertice %d\n", c, v);
                    if(isVariableInDomain(localD, v, c)){
                        // System.out.println("Domain has the color");
                        tmpD = setColor(localD, v, c);
                        prune(tmpD, v, c);
                        if(consistent(tmpD)){
                            // System.out.println("Assignment is consistent, c is " + c);
                            break;
                        }
                    }
                }while(++c <= colors);

                // System.out.printf("v %d, i %d, c %d, colors %d, minColor %d\n", v, i, c, colors, this.minColor);

                if(c > this.minColor || c > colors){
                    pop = true;
                }
                else if(i < this.nodeCount-1){
                    // System.out.println("Stacking new domain");
                    localD   = tmpD;
                    colors   = c == colors ? colors : colors - 1;
                    stack.push(new DomainStore(tmpD, colors, i, c));

                    // reset color for next variable
                    c = 0;
                    i++;

                }
                else{
                    colors   = c == colors ? colors : colors - 1;
                    // new best solution found
                    // System.out.println("\t\tnew solution found?");
                    if(!this.solutionFound && colors <= this.minColor){
                        // System.out.println("Yes, a new one");
                        // System.out.println("Stack size: " + stack.size());
                        this.minColor = colors;
                        this.solution = tmpD;
                        this.solutionFound = true;
                    }
                    else if(colors < this.minColor){
                        // System.out.println("Yes, a better one");
                        // System.out.println("Stack size: " + stack.size());
                        this.minColor = colors;
                        this.solution = tmpD;
                    }
                    // else
                        // System.out.println("nope");

                    pop = true;
                }
            
            }
            else{
                // System.out.println("last else");
                pop = true;
            }
        }

        System.out.printf("Total iterations are %d\n", it);

        if(this.solutionFound){
            this.domain = localD;
            return true;
        }
        else{
            // System.out.println("exits");
            // System.exit(1);
            this.minColor = stepUp(this.minColor, this.nodeCount);
            return solver();
        }
    }


    boolean isVariableInDomain(ArrayList<LinkedList<Integer>> d, int n, int v){
        return d.get(n).contains(v);
    }

    ArrayList<LinkedList<Integer>> setColor(ArrayList<LinkedList<Integer>> d, int n, int c){
        d.set(n, new LinkedList<Integer>());
        d.get(n).add(c);

        return d;
    }

    boolean consistent(ArrayList<LinkedList<Integer>> tmpD){
        boolean consistent = true;

        // check that color(v) != color(u) when there exists an edge (u,v)
        for(int v = 0; v < tmpD.size(); v++) {
            LinkedList<Integer> d = tmpD.get(v);
            if(d.size() < 1)
                return false;
            else if(d.size() == 1){
                for (int u : edgeList.get(v)) {
                    if(tmpD.get(u).size() == 1 && 
                       tmpD.get(u).getFirst() == d.getFirst()){
                        return false;
                       }
                }
            }
        }

        return consistent;
    }
    void prune(ArrayList<LinkedList<Integer>> tmpD, int n, int c){
        // Prune
        // Make stack! if removal leads to a single domain, 
        // add that variabel to the stack so that its neighbouring vertices are pruned
        LinkedList<int[]> stack = new LinkedList<int[]>();
        stack.push(new int[]{n, c});
        int i = 0;
        while(!stack.isEmpty()){
            i++;
            // printArrayList(tmpD);
            if(i > 10)
                break;
            int[] pair = stack.pop();
            // System.out.println("prune round: " + i);
            // System.out.printf("vertice %d has been assigned color %d\n", pair[0], pair[1]);
            for (int u : edgeList.get(pair[0])) {
                if( tmpD.get(u).removeFirstOccurrence(pair[1]))
                    if(tmpD.get(u).size() == 1)
                        stack.push(new int[]{u, tmpD.get(u).get(0)});
            }
        }

    }

    void sortOrderByGrade(){
        int[] grade = new int[nodeCount];
        for (int i = 0; i < this.edgeList.size(); i++) {
            grade[i] = this.edgeList.get(i).size(); 
            this.orderByGrade[i] = i;
        }
        // simple insertion sort
        int tmp_grade; 
        int tmp_order;

        for(int i = 1; i < nodeCount; i++){
            tmp_grade = grade[i];
            tmp_order = this.orderByGrade[i];
            int j = i-1;
            // descending order
            while(j >= 0 && grade[j] < tmp_grade){
                // move relative value
                grade[j+1] = grade[j];
                // move order
                this.orderByGrade[j+1] = this.orderByGrade[j];
                j--;
            }
            grade[j+1] = tmp_grade;
            this.orderByGrade[j+1] = tmp_order;
        }

    }


    public void printArrayList(ArrayList<LinkedList<Integer>> list){
        for(int i = 0; i < list.size(); i++){
            System.out.print(i + " [");
            for (int e : list.get(i)) {
                System.out.print(e + " ");
            }
            System.out.println("]");
        }
    }


}
