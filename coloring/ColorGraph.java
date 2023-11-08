import java.util.ArrayList;
import java.util.LinkedList;

public class ColorGraph {
    ArrayList<LinkedList<Integer>> edgeList;
    ArrayList<LinkedList<Integer>> solution = null;

    boolean solutionFound = false;
    int minimumValues;
    int nrOfVariables;

    public ColorGraph(ArrayList<LinkedList<Integer>> edgeList, int nc){
        this.nrOfVariables = nc;
        this.minimumValues = nc;
        this.edgeList = edgeList;
    }

    /*
     *  GETTERS N' SETTERS
     */

    public ArrayList<LinkedList<Integer>> getEdgeList(){
        return this.edgeList;
    }
    public boolean hasSolution(){
        return this.solutionFound;
    }
    public ArrayList<LinkedList<Integer>> getSolution(){
        // return Operations.reorderDomainLists(this.solution, this.mapOrder);
        return this.solution;
    }

    public int getMaxValue(){
        int maxV = 0;
        for (LinkedList<Integer> list : this.solution) {
            maxV = list.getFirst() > maxV ? list.getFirst() : maxV;
        }

        return maxV + 1;
    }

    /*
     *  LOGIC 
     */
    
    public boolean solve(){
        long startTime = System.nanoTime();

        int length = this.edgeList.size();
        int minutes = 0;
        if (length > 500){
            minutes = 1;
        }
        else if(length < 50){
            minutes = 4;
        }
        else{
            minutes = 2;
        }

        long abortTimeShort  = (minutes) * 60 * 1000000000L;
        long abortTimeRandom = (8-minutes) * 60 * 1000000000L;

        //  Greedy Heurestics
        // > Choose Vertice after order
        // > > Highest Grade First
        int[] orderHGF = Operations.sortOrderByGrade(edgeList);

        // > > > Variable First
        // System.out.println("1.1.1");
        DomainStore ds = solveVariableFirst(this.edgeList, orderHGF, this.minimumValues);
        if(ds.solved() && ds.getUsedValues() < this.minimumValues){
            this.minimumValues = ds.getUsedValues();
            this.solution = ds.getdomainList();
            this.solutionFound = true;
            // System.out.println("new solution found, new min value: " + this.minimumValues);
        }

        // > > > Value First
        // System.out.println("1.1.2");
        ds = solveValueFirst(this.edgeList, orderHGF, this.minimumValues);
        if(ds.solved() && ds.getUsedValues() < this.minimumValues){
            this.minimumValues = ds.getUsedValues();
            this.solution = ds.getdomainList();
            this.solutionFound = true;
            // System.out.println("new solution found, new min value: " + this.minimumValues);
        }

        // > > > Most Saturated First
        // System.out.println("1.1.3");
        ds = solveSatFirst(this.edgeList, orderHGF, this.minimumValues);
        if(ds.solved() && ds.getUsedValues() < this.minimumValues){
            this.minimumValues = ds.getUsedValues();
            this.solution = ds.getdomainList();
            this.solutionFound = true;
            // System.out.println("new solution found, new min value: " + this.minimumValues);
        }

        // > > SmallestDegreeLast
        int[] orderSDL = Operations.smallestDegreeLast(edgeList);

        // > > > Variable First
        // System.out.println("1.2.1");
        ds = solveVariableFirst(this.edgeList, orderSDL, this.minimumValues);
        if(ds.solved() && ds.getUsedValues() < this.minimumValues){
            this.minimumValues = ds.getUsedValues();
            this.solution = ds.getdomainList();
            this.solutionFound = true;
            // System.out.println("new solution found, new min value: " + this.minimumValues);
        }

        // > > > Value First
        // System.out.println("1.2.2");
        ds = solveValueFirst(this.edgeList, orderSDL, this.minimumValues);
        if(ds.solved() && ds.getUsedValues() < this.minimumValues){
            this.minimumValues = ds.getUsedValues();
            this.solution = ds.getdomainList();
            this.solutionFound = true;
            // System.out.println("new solution found, new min value: " + this.minimumValues);
        }

        // > > > Most Saturated First
        // System.out.println("1.2.3");
        ds = solveSatFirst(this.edgeList, orderSDL, this.minimumValues);
        if(ds.solved() && ds.getUsedValues() < this.minimumValues){
            this.minimumValues = ds.getUsedValues();
            this.solution = ds.getdomainList();
            this.solutionFound = true;
            // System.out.println("new solution found, new min value: " + this.minimumValues);
        }

        startTime = System.nanoTime();
        // System.out.println("Random checks");
        while(System.nanoTime()-startTime < abortTimeRandom){
            int[] sortOrder = Operations.randomSortOrder(edgeList.size());
            // > > > Variable First
            ds = solveVariableFirst(this.edgeList, sortOrder, this.minimumValues);
            if(ds.solved() && ds.getUsedValues() < this.minimumValues){
                this.minimumValues = ds.getUsedValues();
                this.solution = ds.getdomainList();
                this.solutionFound = true;
                // System.out.println("new solution found, new min value: " + this.minimumValues);
            }

            // > > > Value First
            ds = solveValueFirst(this.edgeList, sortOrder, this.minimumValues);
            if(ds.solved() && ds.getUsedValues() < this.minimumValues){
                this.minimumValues = ds.getUsedValues();
                this.solution = ds.getdomainList();
                this.solutionFound = true;
                // System.out.println("new solution found, new min value: " + this.minimumValues);
            }

            // > > > Most Saturated First
            ds = solveSatFirst(this.edgeList, sortOrder, this.minimumValues);
            if(ds.solved() && ds.getUsedValues() < this.minimumValues){
                this.minimumValues = ds.getUsedValues();
                this.solution = ds.getdomainList();
                this.solutionFound = true;
                // System.out.println("new solution found, new min value: " + this.minimumValues);
            }
        }
        // System.out.println("Time taken: " + ((System.nanoTime()-startTime) / 1000000000L) + " s.");


        // > Exhaustive search
        // System.out.println("2. Exhaust search");
        LinkedList<DomainStore> stack = new LinkedList<>();
        stack.push(new DomainStore(this.edgeList.size(), 0, this.minimumValues));
        startTime = System.nanoTime();
        ds = solveExhaustive(stack, this.minimumValues, startTime, abortTimeShort);
        // System.out.println("Time taken: " + ((System.nanoTime()-startTime) / 1000000000L) + " s.");

        if(ds.solved() && ds.getUsedValues() < this.minimumValues){
            this.minimumValues = ds.getUsedValues();
            this.solution = ds.getdomainList();
            this.solutionFound = true;
            // System.out.println("new solution found, new min value: " + this.minimumValues);
        }

        if((System.nanoTime() - startTime) > abortTimeShort)
            return false;
        else
            return true;
        
    }

    private DomainStore solveExhaustive(LinkedList<DomainStore> stack, int target, long startTime, long abortTimeNanoSeconds) {
        DomainStore best = stack.getFirst();
        // int it = 0;
        while(!stack.isEmpty()){

            if(System.nanoTime() - startTime > abortTimeNanoSeconds){
                // System.out.println("Times up, it: " + it);
                return best;
            }
            
            DomainStore ds = stack.pop();

            // if(it > 10)
            //     return best;
            // else{
            //     Operations.printArrayList(ds.getdomainList());
            // }

            // it++;
            // Get next Decision Variable
            int variable = ds.getNextVariable();
            int currentMin = ds.getUsedValues();
            // System.out.printf("Current Variable is %d, currentMin is %d, target is %d\n", variable, currentMin, target);

            if(variable >= ds.size()){
                // System.out.println("variable >= ds.Size()");
                // Check if new best distrobution
                if(ds.solved()){
                    best = ds;
                    target = currentMin;
                    stack = cleanStack(stack, target);
                }
                else if(currentMin < target){
                    ds.setVariable(0);
                    stack.push(ds);
                }

            }
            else if(currentMin < target){
                // System.out.println("currentMin < target");
                    
                //  Get next value
                int value = 0;
                while(!Operations.consistent(ds.getdomainList(), this.edgeList, variable, value) && value < target){
                    value++;
                };

                // System.out.println("value for consistency: " + value);

                if(value < target){
                    // System.out.println("value < target");

                    // 2. Remove the current value, push if consistent
                    DomainStore tmpDS = new DomainStore(ds);
                    if(tmpDS.removeValue(variable, value) && Operations.consistent(tmpDS.getdomainList(), this.edgeList, variable)){
                        tmpDS.setVariable(variable + 1);;
                        stack.push(tmpDS);
                        // System.out.println("Pushed 1");
                    }
    
                    // 3. Decide the variable to the current value, push if consistent
                    if(Operations.consistent(ds.getdomainList(), this.edgeList, variable, value)){
                        ds.setdomainList(Operations.setColor(ds.getdomainList(), variable, value));
                        ds.setVariable(0);
                        stack.push(ds);
                        // System.out.println("Pushed 2");

                    }
                }
            }
        }

        return best;
    }

    private LinkedList<DomainStore> cleanStack(LinkedList<DomainStore> stack, int target) {
        if(stack.isEmpty())
            return stack;

        DomainStore ds = stack.pop();
        while(ds.getUsedValues() >= target){
            ds = stack.pop();
        }
        stack.push(ds);

        return stack;
    }

    // Iterates through the vertices in the order specified by sortOrder
    // Starts with value = 0, all eligable vertices are given this value in order
    // If value gets larger than minValue the loop exits
    // If all vertices have been assigned a value the loop exits
    // Otherwise, value is increased by 1 and the next eligable vertices are assigned this value in order
    // Solved or no, the method returnes the DomainStore 
    DomainStore solveValueFirst(ArrayList<LinkedList<Integer>> edgeList, int[] sortOrder, int minValue){
        DomainStore ds = new DomainStore(this.edgeList.size(), minValue, sortOrder);
        int value = 0;

        while(value <= minValue && Operations.levelOfCompletetion(ds) < ds.size()){
            for(int i = 0; i < ds.size(); i++){
                int variable = sortOrder[i];
                Operations.setValueToVariable(variable, value, ds, edgeList);
            }

            value++;
        }

        return ds;
    }

    // Iterates through the vertices in the order specified by sortOrder
    // Each vertice is giving the first, consistent value that is within its domain
    // If no eligble value is found for a vertice, the method exits by returning the current DomainStore
    // If all vertices successfully assigned a value the DomainStore is returned
    DomainStore solveVariableFirst (ArrayList<LinkedList<Integer>> edgeList, int[] sortOrder, int minValue){
        DomainStore ds = new DomainStore(edgeList.size(), minValue, sortOrder);

        for(int i = 0; i < ds.size(); i++){
            int variable = sortOrder[i];
            int decidedValue = -1;
            for (int value : ds.domainList.get(variable)) {
                if(Operations.setValueToVariable(variable, value, ds, this.edgeList)){
                    decidedValue = value;
                    break;
                }
            }
            if(decidedValue == -1){
                return ds;
            }
        }

        return ds;
    }

    DomainStore solveSatFirst(ArrayList<LinkedList<Integer>> edgeList, int[] sortOrder, int minValue) {
        DomainStore ds = new DomainStore(edgeList.size(), minValue, sortOrder);

        while(Operations.levelOfCompletetion(ds) < ds.size()){
            int value = 0;
            int variable = Operations.getMostSaturated(edgeList, ds.domainList, sortOrder);
            if(variable == -1){
                System.err.println("Got -1...");
                System.exit(1);
            }
            while(!Operations.setValueToVariable(variable, value, ds, edgeList)){
                value++;
                if(value > minValue)
                    return ds;
            }

        }

        return ds;
    }

}
