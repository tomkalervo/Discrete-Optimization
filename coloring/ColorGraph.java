import java.util.ArrayList;
import java.util.LinkedList;
import java.util.stream.IntStream;

public class ColorGraph {
    int[] orderByGrade;
    ArrayList<LinkedList<Integer>> edgeList;
    LinkedList<Integer> singleEdgeList;

    ArrayList<LinkedList<Integer>> domain;
    ArrayList<LinkedList<Integer>> solution = null;
    boolean solutionFound = false;
    int minColor;
    int nodeCount;

    public ColorGraph(ArrayList<LinkedList<Integer>> n, int nc){
        this.singleEdgeList = new LinkedList<Integer>();
        int i = 0;
        for (; i < n.size(); i++) {
            if(n.get(i).size() == 1){
                singleEdgeList.push(i);
            }
        }
        this.edgeList = n;
        this.nodeCount = nc;
        this.minColor = nc;
        this.orderByGrade = new int[nodeCount];
        sortOrderByGrade();
        i = 0;
        this.minColor = decideStartColorTarget(this.nodeCount, this.edgeList);
        this.domain = setUpDomain(nc, this.singleEdgeList, this.orderByGrade, this.minColor);
    }

    private ArrayList<LinkedList<Integer>> setUpDomain
        (int nodeCount, LinkedList<Integer> singleEdgeList, int[] order, int target) {
        ArrayList<LinkedList<Integer>> domain = new ArrayList<LinkedList<Integer>>();
        for(int i = 0; i < nodeCount; i++){
            domain.add(null);
        }
        LinkedList<Integer> sList = 
            new LinkedList<>(singleEdgeList).reversed();
            
        if(singleEdgeList.isEmpty()){
            for(int i = 0; i < nodeCount; i++){
                int dMax = target < i ? target : i;
                domain.set(order[i], 
                    new LinkedList<Integer>(
                    IntStream
                    .range(0, dMax+1)
                    .boxed()
                    .toList()));
            }
        }
        else{

            int s = sList.pop();
            int dMax = 1;
            int i = 0;
            do{
                int k = i;
                while(i < s){
                    domain.set(order[i], 
                    new LinkedList<Integer>(
                        IntStream
                        .range(0, dMax+1)
                        .boxed()
                        .toList()));
                        dMax = dMax < target ? dMax+1 : target;
                        i += 1;
                    }
                    
            while(i == s){
                domain.set(order[i], 
                new LinkedList<Integer>(
                    IntStream
                    .range(0, 2)
                    .boxed()
                    .toList()));
                    
                    if(!sList.isEmpty())
                    s = sList.pop();
                    i += 1;
                }
                
                if(i == k){
                    System.err.println("Bad logic, index i not incremented\n");
                    System.exit(1);
                }
                
            }while(i < nodeCount);
        }
        return domain;
    }
                
    class DomainStore{
        ArrayList<LinkedList<Integer>> domain;
        int usedValues;
        int variable;
        boolean[] visited;

        /**
         * 
         * @param d Contains the current domain of values related to variables
         * @param usedV The sum of used/assigned values
         * @param v The next variable
         * @param iterator The amount of iteration
         */
        public DomainStore(ArrayList<LinkedList<Integer>> d, int usedV, int v){
            this.domain = new ArrayList<LinkedList<Integer>>(d);
            this.usedValues = usedV;
            this.variable = v;
            this.visited = new boolean[d.size()];
            for (int i = 0; i < d.size(); i++) {
                 visited[i] = false;
            }
        }
    }

    private int decideStartColorTarget(int v, ArrayList<LinkedList<Integer>> elist){
        int e = 0;
        int target = 1;
        for (LinkedList<Integer> list : elist) {
            e += list.size();
        }
        e = e >> 1;
        int maxE = getEdgesCompleteGraph(v);
        float fraction  = e / (float)maxE;
        target = Math.round(fraction * v);
        return target;
    }

    private int stepUp(int prevGoal, int vertices){
        int fraction = (vertices / 10);
        int newGoal = prevGoal + fraction > prevGoal ? prevGoal + fraction : prevGoal++;
        this.domain = setUpDomain(this.nodeCount, this.singleEdgeList, this.orderByGrade, newGoal);
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
    
    public boolean solve(){
        long startTime = System.nanoTime();
        long abortTimeNanoSeconds = 1 * 60000000000L;

        this.domain = solver(domain, minColor, startTime, abortTimeNanoSeconds);
        if(System.nanoTime() - startTime > abortTimeNanoSeconds){
            return false;
        }
        else
            return true;
    }

    ArrayList<LinkedList<Integer>> solver(ArrayList<LinkedList<Integer>> domain, int target, long startTime, long abortTime){
        int minimum = target+1;

        LinkedList<DomainStore> stack = new LinkedList<>();
        stack.push(new DomainStore(domain, 1, this.orderByGrade[0]));
        while(!stack.isEmpty()){
            if(stack.getFirst().variable == -1){
                // solution found
                DomainStore ds = stack.pop();
                if(ds.usedValues < minimum){
                    // new best solution
                    minimum = ds.usedValues;
                    domain = ds.domain;
                    this.solutionFound = true;
                    this.solution = ds.domain;
                    this.minColor = minimum;
                }
            }
            if(System.nanoTime() - startTime > abortTime)
                return domain;
            
            stack = solveStack(stack, orderByGrade, minimum);
        }

        // System.out.println("stack empty, ending solver");
        if(!this.solutionFound){
            return solver(domain, stepUp(target, domain.size()), startTime, abortTime);
        } 
        else{
            return domain;
        }
    }

    LinkedList<DomainStore> solveStack(LinkedList<DomainStore> stack, int[] order, int target){
        if(stack.isEmpty()){
            return stack;
        }
        DomainStore                    start    = stack.pop();
        ArrayList<LinkedList<Integer>> localD   = start.domain;
        int                            colors   = start.usedValues;
        int                            variable = start.variable;
        int                            value    = 0;
        boolean                        consistent = false;
        
        start.visited[variable] = true;

        // no better solution to be found in this branch
        if(colors >= target)
            return stack;

        // check if any vertice have not been tried at this level
        int vNext = nextV(localD, order, start.visited, start.usedValues);
        if(vNext >= localD.size()){
            // start.usedValues++;
            vNext = nextV(localD, order, start.visited, start.usedValues + 1);
        }

        if(vNext < localD.size()){
            start.variable = vNext;
            // Build stack sideways
            stack.push(start);
        }

        
        // handle current variable
        while(!consistent){
            if(value >= target){
                return stack;
            }

            if(isVariableInDomain(localD, variable, value)){
                ArrayList<LinkedList<Integer>> tmpD = copyDomain(localD);
                tmpD = setColor(localD, variable, value);
                prune(tmpD, variable, value);
                if(consistent(tmpD)){
                    // for (int u : edgeList.get(variable)) {
                    //     if(tmpD.get(u).size() > 1){
                    //         int v_i = 0;
                    //         while(!isVariableInDomain(tmpD, u, v_i) && v_i < colors){
                    //             v_i++;
                    //         }
                    //         if(v_i < colors){
                    //             tmpD = setColor(localD, u, v_i);
                    //             prune(tmpD, u, v_i);

                    //         }
                    //     }
                    // }
                    // if(!consistent(tmpD)){
                    //     System.err.println("Bad logic at 260");
                    //     System.exit(1);
                    // }

                    localD = tmpD;
                    consistent = true;
                    colors = value > colors ? value : colors;
                }
            }
            
            value++;            
        }

        if(solution(localD)){
            stack.push(new DomainStore(localD, colors, -1));
            return stack;
        }

        boolean[] visited = getVisited(localD);
        vNext = nextV(localD, order, visited, colors);

        if(vNext >= localD.size()){
            colors++;
            vNext = nextV(localD, order, visited, colors);
        }
        
        // build stack downwards, with new iterator
        if(vNext < localD.size()){
            stack.push(new DomainStore(localD, colors, vNext));
        }

        return stack;
    }


    ArrayList<LinkedList<Integer>> copyDomain(ArrayList<LinkedList<Integer>> localD) {
        ArrayList<LinkedList<Integer>> tmpD = new ArrayList<LinkedList<Integer>>();
        for (int j = 0; j < localD.size(); j++) {
            tmpD.add(j, new LinkedList<Integer> (localD.get(j)));
        }
        return tmpD;	
    }

	boolean[] getVisited(ArrayList<LinkedList<Integer>> localD) {
        boolean[] visited = new boolean[localD.size()];
        for(int i = 0; i < localD.size(); i++){
            visited[i] = localD.get(i).size() == 1 ? true : false;
        }
		return visited;
	}

    boolean solution(ArrayList<LinkedList<Integer>> localD){
        int i = 0;
        for (LinkedList<Integer> list : localD) {
            i += list.size();
        }
        return i == localD.size() ? true : false;
    }

	boolean needAnotherColor(ArrayList<LinkedList<Integer>> localD, int n, int colors) {
        for(int i = 0; i <= colors; i++){

            if(isVariableInDomain(localD, n, i)){
                ArrayList<LinkedList<Integer>> tmpD = copyDomain(localD);
                tmpD = setColor(tmpD, n, i);

                prune(tmpD, n, i);
                if(consistent(tmpD)){
                    return false;
                }

            }
        }
		return true;
	}

    /**
     * 
     * @param d The domains
     * @param n The variable
     * @param v The value
     * @return true if v is in the domain of n
     */
	boolean isVariableInDomain(ArrayList<LinkedList<Integer>> d, int n, int v){
        return d.get(n).contains(v);
    }

    /**
     * 
     * @param d The list of domains
     * @param n The variable
     * @param v The value 
     * @return updated list of domains where the domain of n now only contain v
     */
    ArrayList<LinkedList<Integer>> setColor(ArrayList<LinkedList<Integer>> d, int n, int v){
        d.set(n, new LinkedList<Integer>());
        d.get(n).add(v);

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
                        System.out.println("warning 1");
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
            for (int u : edgeList.get(pair[0])) {
                if( tmpD.get(u).removeFirstOccurrence(pair[1]))
                    if(tmpD.get(u).size() == 1)
                        stack.push(new int[]{u, tmpD.get(u).get(0)});
            }
        }

    }

    int nextV(ArrayList<LinkedList<Integer>> tmpD, int[] order, boolean[] visited, int colors){
        int k = 0;
        for (; k < tmpD.size(); k++) {
            if((!visited[order[k]]) && (tmpD.get(order[k]).size() != 1)){
                if(!needAnotherColor(tmpD, order[k], colors)){
                    break;
                }
            }
        }
        if(k < tmpD.size())
            return order[k];
        else
            return k;
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
