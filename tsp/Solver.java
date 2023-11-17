import java.io.*;
import java.util.List;
import java.util.ArrayList;

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
        String firstLine = lines.get(0);
        int nodeCount = Integer.parseInt(firstLine);
        // System.out.println("Nodes: " + nodeCount);
        // int edgeCount = Integer.parseInt(firstLine[1]);

        ArrayList<double[]> nodes = new ArrayList<>();
        for (int i = 1; i <= nodeCount; i++) {
            String[] parts = lines.get(i).split("\\s+");
            double[] cp = {Double.parseDouble(parts[0]), Double.parseDouble(parts[1])};
            nodes.add(cp);
        }

        Path p = TravelingSalesman.solve(nodeCount, nodes);
        double totalDistance = p.getTotalDistance();
        // System.out.println("Old:");
        System.out.printf("%f %d\n", totalDistance, 0);
        for (int n : p.getPath()) {
            System.out.printf("%d ", n);
        }
        System.out.println("");  
        
        // p = TravelingSalesman.solvePermutation(nodeCount, nodes);
        // totalDistance = p.getTotalDistance();
        // System.out.println("New:");
        // System.out.printf("%f %d\n", totalDistance, 0);
        // for (int n : p.getPath()) {
        //     System.out.printf("%d ", n);
        // }
        // System.out.println("");  
        
        // View v = new View(nodes, p);
        // v.drawPoints(nodes);

        //System.exit(0);
 
    }
}