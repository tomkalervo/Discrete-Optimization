import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Solver {
    public static void main(String[] args) {
        try {
            solve(args);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

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
        int n = Integer.parseInt(firstLine.split("\\s")[0]);
        int m = Integer.parseInt(firstLine.split("\\s")[1]);

        System.out.printf("Total amount of customers are %d, total amount of facilities are %d.\n", m, n);

        // Read facilities
        // ArrayList<int[]> facility = new ArrayList<>();
        int i = 0;
        for (int f = 0; f < n; f++) {
            i += 1;
            String[] parts = lines.get(i).split("\\s+");
            System.out.printf("Facility %d: \tsetup cost = %d \tcapacity = %d \tlocation = (%.2f,%.2f)\n", 
                                f, Integer.parseInt(parts[0]),Integer.parseInt(parts[1]), Double.parseDouble(parts[2]), Double.parseDouble(parts[3]));
            // facility.add(cp);
        }
        for (int c = 0; c < m; c++) {
            i += 1;
            String[] parts = lines.get(i).split("\\s+");
            System.out.printf("Customer %d: \tdemand = %d \tlocation = (%.2f,%.2f)\n", 
                                c, Integer.parseInt(parts[0]), Double.parseDouble(parts[1]), Double.parseDouble(parts[2]));
            // facility.add(cp);
        }
        

        // Print solution
        // System.out.printf("%f %d\n", totalDistance, 0);
        // for (int n : p.getPath()) {
        //     System.out.printf("%d ", n);
        // }
        // System.out.println("");  
    }

}
