import java.io.*;
import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;

/**
 * The class <code>Solver</code> is an implementation of a greedy algorithm to solve the knapsack problem.
 *
 */
public class Solver {
    
    /**
     * The main class
     */
    public static void main(String[] args) {
        try {
            solve(args);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Read the instance, solve it, and print the solution in the standard output
     */
    public static void solve(String[] args) throws IOException {
        String fileName = null;
        
        // get the temp file name
        for(String arg : args){
            if(arg.startsWith("-file=")){
                fileName = arg.substring(6);
            } 
        }
        if(fileName == null)
            return;
        
        // read the lines out of the file
        List<String> lines = new ArrayList<String>();

        BufferedReader input =  new BufferedReader(new FileReader(fileName));
        try {
            String line = null;
            while (( line = input.readLine()) != null){
                lines.add(line);
            }
        }
        finally {
            input.close();
        }
        // parse the data in the file
        String[] firstLine = lines.get(0).split("\\s+");
        int nodeCount = Integer.parseInt(firstLine[0]);
        int edgeCount = Integer.parseInt(firstLine[1]);

        ArrayList<LinkedList<Integer>> nodes = new ArrayList<>();
        int i = nodeCount;
        while (i-- > 0) {
            nodes
                .add(new LinkedList<>());
        }

        for(i=1; i < edgeCount+1; i++){
        String line = lines.get(i);
        String[] parts = line.split("\\s+");
            nodes
            .get(Integer.parseInt(parts[0]))
            .addFirst(Integer.parseInt(parts[1]));

            nodes
            .get(Integer.parseInt(parts[1]))
            .addFirst(Integer.parseInt(parts[0]));
        }

        // System.out.println("Start new ColorGraph");
        ColorGraph cg = new ColorGraph(nodes, nodeCount);
        boolean solved = cg.solver();
        /**
         * Improvments to implement: 
         * Store the sorting order in an array (no need to redo the sorting)
         * Increase domain (colors) during search
         * Iterate backwards to make an exhaustive search
         * Store best result (stop search if solution require more colors than the best)
         * Add timelimit if needed
         */

        if(solved){
            System.out.printf("%d %d\n", cg.minColor+1, 1);
            for (LinkedList<Integer> v : cg.getSolution()) {
                System.out.printf("%d ", v.getFirst());
            }
            System.out.println("");  
        }
        else{
            if(cg.getSolution() == null){
                System.out.println("No solution found before time out");
                System.exit(1);
            }
            System.out.printf("%d %d\n", cg.minColor+1, 0);
            for (LinkedList<Integer> v : cg.getSolution()) {
                System.out.printf("%d ", v.getFirst());
            }
            System.out.println("");  
        }
 
    }
}