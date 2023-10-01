import java.io.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
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
        // System.out.println("Start of parse, firstLine: " + lines.get(0));
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

        // System.out.println("Nodes: " + nodeCount + ", Edges: " + edgeCount);
        // System.out.println("Length of ArrayList nodes: " + nodes.size());
        for(i=1; i < edgeCount+1; i++){
        // System.out.println("parse of line: " + i + ": " + lines.get(i));
        String line = lines.get(i);
        String[] parts = line.split("\\s+");
            nodes
            .get(Integer.parseInt(parts[0]))
            .addFirst(Integer.parseInt(parts[1]));

            nodes
            .get(Integer.parseInt(parts[1]))
            .addFirst(Integer.parseInt(parts[0]));

            // System.out.printf("node_list %d w/ %d and node_list %d w/ %d\n", 
            //     Integer.parseInt(parts[0]), nodes.get(Integer.parseInt(parts[0])).getFirst()
            //    ,Integer.parseInt(parts[1]), nodes.get(Integer.parseInt(parts[1])).getFirst());
        }

        // for (int v = 0; v < nodeCount; v++) {
        //     System.out.printf("v: %d, edge to [", v);
        //     LinkedList<Integer> neighbours = nodes.get(v);
        //     for (int u : neighbours) {
        //             System.out.printf("%d ", u);
        //     }
        //     System.out.printf("]\n");

        // }
 
    }
}