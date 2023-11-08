import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Random;
import java.util.random.RandomGenerator;

public class Operations {

   public static ArrayList<LinkedList<Integer>> orderEdgeList(ArrayList<LinkedList<Integer>> edgeList, int[] order, int[] mapOrder) {
        ArrayList<LinkedList<Integer>> orderedEdgeList = new ArrayList<LinkedList<Integer>>();
        for(int i = 0; i < edgeList.size(); i++){
            int vertice = order[i];
            // System.out.printf("vertice %d is at order %d\n", i, order[i]);
            LinkedList<Integer> edges = new LinkedList<Integer>();
            for (int neighbouringVertice : edgeList.get(vertice)) {
                edges.addLast(mapOrder[neighbouringVertice]);
            }
            Collections.sort(edges);
            orderedEdgeList.addLast(edges);
        }
		return orderedEdgeList;
	}

    public static int[] sortOrderByGrade(ArrayList<LinkedList<Integer>> edgeList){
        int[] order = new int[edgeList.size()];
        int[] grade = new int[edgeList.size()];
        for (int i = 0; i < edgeList.size(); i++) {
            grade[i] = edgeList.get(i).size(); 
            order[i] = i;
        }
        // simple insertion sort
        int tmp_grade; 
        int tmp_order;

        for(int i = 1; i < edgeList.size(); i++){
            tmp_grade = grade[i];
            tmp_order = order[i];
            int j = i-1;
            // descending order
            while(j >= 0 && grade[j] < tmp_grade){
                // move relative value
                grade[j+1] = grade[j];
                // move order
                order[j+1] = order[j];
                j--;
            }
            grade[j+1] = tmp_grade;
            order[j+1] = tmp_order;
        }
        return order;
    }

    /**
     * 
     * @param edgeList
     * @param nodes
     * @return A lower bound of values indicates the minimum colors needed to color a 
     * connected graph based on the amount of edges. In the * actual graph, 
     * a higher value of colors might be needed.
     */
    public static int calculateLowerBound(ArrayList<LinkedList<Integer>> edgeList) {
        int nodes = edgeList.size();
        int edges = 0;
        int values = 0;
        for (LinkedList<Integer> list : edgeList) {
            edges += list.size();
        }
        edges = edges >> 1;
        // System.out.printf("Edges: %d, Nodes: %d\n", edges, nodes);
        edges -= (nodes/2) * (nodes/2);
        values = 2;
        int i = 0;
        while(edges >= 0){
            i++;
            edges -= (nodes/2) - i;
            if(edges >= 0)
                values++;
            edges -= (nodes/2) - i;
            if(edges >= 0)
                values++;
        }

		return values;
	}

    /**
     * 
     * @param variable
     * @param value
     * @param ds
     * @param edgeList
     * @return returns true if consistent: sets the decision variable to the value. Prunes the searchspace. 
     */
    public static boolean setValueToVariable(
        int variable, int value, DomainStore ds, ArrayList<LinkedList<Integer>> edgeList){
        if(!consistent(ds.domainList, edgeList, variable, value))
            return false;

        ArrayList<LinkedList<Integer>> tmpD 
            = ds.getdomainList();

        setColor(tmpD, variable, value);
        if (prune(tmpD, edgeList, variable, value)){
            // System.out.println("Consistent: ");
            // printArrayList(tmpD);
            ds.setdomainList(tmpD);
            return true;
        }
        else{
            // System.out.println("Not Consistent: ");
            // printArrayList(tmpD);
            return false;
        }
    }

    /**
     * 
     * @param domainList The domains
     * @param variable The variable
     * @param value The value
     * @return true if v is in the domain of n
     */
	static boolean isVariableInDomain(ArrayList<LinkedList<Integer>> domainList, int variable, int value){
        return domainList.get(variable).contains(value);
    }

    /**
     * 
     * @param domainList The list of domains
     * @param variable The variable
     * @param value The value 
     * @return updated list of domains where the domain of variable now only contain value
     */
    static ArrayList<LinkedList<Integer>> setColor(ArrayList<LinkedList<Integer>> domainList, int variable, int value){
        // System.out.println("Sets color " + value + " to variable " + variable);
        domainList.set(variable, new LinkedList<Integer>());
        domainList.get(variable).add(value);
        // Operations.printArrayList(domainList);
        // System.out.println("returns the list");
        return domainList;
    }

    static boolean consistent(ArrayList<LinkedList<Integer>> domainList, ArrayList<LinkedList<Integer>> edgeList, int variable, int value){
        if(!isVariableInDomain(domainList, variable, value))
            return false;
        // check that color(v) != color(u) when there exists an edge (u,v)
        for (int connectedVariable : edgeList.get(variable)) {
            if(domainList.get(connectedVariable) == null){
                System.err.println("Error in consistent checking");
                System.err.println("domain of variable " + connectedVariable + " is null");
                Operations.printArrayList(domainList);
            }
            if(domainList.get(connectedVariable).size() == 1 
                && domainList.get(connectedVariable).getFirst() == value)
                return false;
        }
            
        return true;
    }

    static boolean consistent(ArrayList<LinkedList<Integer>> domainList, ArrayList<LinkedList<Integer>> edgeList, int variable){
        LinkedList<Integer> save = new LinkedList<>();

        while(!domainList.get(variable).isEmpty()){
            int value = domainList.get(variable).getFirst();
            if(consistent(domainList, edgeList, variable, value))
                save.addLast(value);
            
            domainList.get(variable).removeFirst();
        }

        domainList.set(variable, save);

        if(domainList.get(variable).size() == 0)
            return false;
        else
            return true;
    }

    static boolean prune(ArrayList<LinkedList<Integer>> domainList, ArrayList<LinkedList<Integer>> edgeList, int variable, int value){
        // Prune
        // Make stack! if removal leads to a single domain, 
        // add that variable to the stack so that its neighbouring vertices are pruned
        LinkedList<int[]> stack = new LinkedList<int[]>();
        stack.push(new int[]{variable, value});

        ArrayList<LinkedList<Integer>> tmpD =
            new ArrayList<LinkedList<Integer>> (domainList);

        while(!stack.isEmpty()){
            int[] pair = stack.pop();
            // System.out.printf("Prune variable %d with value %d\n", pair[0], pair[1]);
            for (int u : edgeList.get(pair[0])) {
                // Checking neighbouring variable u
                // System.out.printf("Variable %d is neighbour to variable %d\n", u, pair[0]);

                if(tmpD.get(u).size() > 1)
                    if(tmpD.get(u).removeFirstOccurrence(pair[1]))
                        if(tmpD.get(u).size() == 1){
                            if(consistent(tmpD, edgeList, u, tmpD.get(u).getFirst()))
                                stack.push(new int[]{u, tmpD.get(u).getFirst()});
                            else
                                return false;
                        }
            }
        }
        domainList = tmpD;
        return true;
    }

    public static int levelOfCompletetion(DomainStore ds) {
        int level = 0;
        for (LinkedList<Integer> values : ds.getdomainList()) {
            level = values.size() == 1 ? level + 1 : level;
        }
		return level;
	}

    public static void printArrayList(ArrayList<LinkedList<Integer>> list){
        for(int i = 0; i < list.size(); i++){
            System.out.print(i + " [");
            if(list.get(i) == null)
                System.out.print("null");
            else
                for (int e : list.get(i)) {
                    System.out.print(e + " ");
                }
            System.out.println("]");
        }
    }

    public static ArrayList<LinkedList<Integer>> reorderDomainLists(ArrayList<LinkedList<Integer>> domainlists, int[] order){
        ArrayList<LinkedList<Integer>> reversedSolution 
            = new ArrayList<LinkedList<Integer>> ();
        for(int i = 0; i < domainlists.size(); i++){
            LinkedList<Integer> reversedEdges = new LinkedList<Integer>();
            int value = domainlists.get(order[i]).getFirst();
            reversedEdges.add(value);
            reversedSolution.add(i, reversedEdges);
        }
        return reversedSolution;
    }

    public static int[] smallestDegreeLast(ArrayList<LinkedList<Integer>> arrayList){
        int[] order = new int[arrayList.size()];

        ArrayList<LinkedList<Integer>> arrayListCopy = 
            new ArrayList<> ();

        for(int i = 0; i < arrayList.size(); i++){
            LinkedList<Integer> list = 
                new LinkedList<>(arrayList.get(i));
            arrayListCopy.addLast(list);
        }

        for(int i = 0; i < arrayListCopy.size(); i++){
            order[i] = getLowestDegree(arrayListCopy);
            arrayListCopy = removeEdges(arrayListCopy, order[i]);
            arrayListCopy.set(order[i],null);
        }

        return order;
    }

    private static ArrayList<LinkedList<Integer>> removeEdges(ArrayList<LinkedList<Integer>> edgeList, int i) {
        // System.out.println("remove " + i + " from ");
        // Operations.printArrayList(edgeList);
        for (LinkedList<Integer> list : edgeList) {
            if(list != null){
                list.removeFirstOccurrence(i);
            }
        }
        // System.out.println("Result: ");
        // Operations.printArrayList(edgeList);
        return edgeList;
    }

    private static int getHighestDegree(ArrayList<LinkedList<Integer>> edgeList) {
        // Vertex w/ Highest Degree
        int vhd = 0;
        int vhd_i = 0;

        for(int i = 0; i < edgeList.size(); i++){
            if(edgeList.get(i) != null){
                if(edgeList.get(i).size() > vhd){
                    vhd = edgeList.get(i).size();
                    vhd_i = i;
                }
            }
        }

        return vhd_i;
    }

    private static int getLowestDegree(ArrayList<LinkedList<Integer>> edgeList) {
        // Vertex w/ Highest Degree
        int vhd = edgeList.size();
        int vhd_i = 0;
        int i = 0;

        while(i < edgeList.size()){
            if(edgeList.get(i) != null){
                if(edgeList.get(i).size() < vhd){
                    vhd = edgeList.get(i).size();
                    vhd_i = i;
                }
            }
            i++;
        }

        return vhd_i;
    }


    public static int getMostSaturated(ArrayList<LinkedList<Integer>> edgeList,
            ArrayList<LinkedList<Integer>> domainList, int[] sortOrder) {
        int vms = -1;
        int maxSat = -1;
        for(int i = 0; i < edgeList.size(); i++){
            // System.out.println("Check of " + sortOrder[i]);
            if(domainList.get(sortOrder[i]).size() > 1){
                int sat = 0;
                for (int u : edgeList.get(sortOrder[i])) {
                    if(domainList.get(u).size() == 1)
                        sat++;
                }
                if(sat > maxSat){
                    // System.out.println("New Max Sat");
                    maxSat = sat;
                    vms = sortOrder[i];
                }
            }
        }

        return vms;
    }

    public static int[] backtrackOrder(int[] sortOrder){
        int[] backtrack = new int[sortOrder.length];
        for(int i = 0; i < sortOrder.length; i++){
            backtrack[sortOrder[i]] = i;
        }

        return backtrack;
    }

    public static int[] randomSortOrder(int size) {
        ArrayList<LinkedList<Integer>> edgeList = new ArrayList<>();
        RandomGenerator rnd = new Random();
        for (int i = 0; i < size; i++) {
            LinkedList<Integer> tmpList = new LinkedList<>();
            // int rndEdges = rnd.nextInt(size);
            // System.out.println(rndEdges);
            for(int j = 0; j < rnd.nextInt(size); j++){
                tmpList.add(j);
            }
            edgeList.add(tmpList);
        }

        return sortOrderByGrade(edgeList);
    }


}
