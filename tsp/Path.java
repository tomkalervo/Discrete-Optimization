import java.util.ArrayList;
import java.util.LinkedList;

public class Path{
    int TABUSIZE = 5;
    ArrayList<double[]> nodes;
    int[] path;
    double[] distance;
    double totalDistance;
    ArrayList<LinkedList<Integer>> tabu;
    public Path(int[] path, ArrayList<double[]> nodes){
        this.nodes = nodes;
        this.path = path;
        this.distance = new double[path.length];
        initDistance();
        tabu = new ArrayList<>();
        for (int i = 0; i < this.nodes.size(); i++){
            tabu.add(new LinkedList<Integer>());
        }
    }
    public Path(Path p){
        this.nodes = p.getNodes();
        this.path = new int[nodes.size()];
        this.distance = new double[nodes.size()];
        for(int i = 0; i < this.path.length; i++){
            this.path[i] = p.getPath()[i];
            this.distance[i] = p.getDistance()[i];
        }
        this.totalDistance = p.getTotalDistance();
        this.tabu = p.getTabu();
    }

    public ArrayList<LinkedList<Integer>> getTabu() {
        return tabu;
    }

    public void addTabu(int point, int position){

        this.tabu.get(point).addFirst(position);
        if(this.tabu.get(point).size() > TABUSIZE){
            this.tabu.get(point).removeLast();
        }

    }

    public boolean isTabu(int point, int position){
        return this.tabu.get(point).contains(position);
    }

    void initDistance(){
        double[] p1;
        double[] p2;
        int i;
        for(i = 0; i < path.length-1; i++){
            p1 = nodes.get(path[i]);
            p2 = nodes.get(path[i+1]);
            distance[i] = Operations.distance(p1, p2);
        }
        p1 = nodes.get(path[i]);
        p2 = nodes.get(path[0]);
        distance[i] = Operations.distance(p1, p2);

        totalDistance = 0;
        for (double d : distance) {
            totalDistance += d;
        }
    }

    public int[] getPath() {
        return path;
    }

    public double getTotalDistance() {
        
        return totalDistance;
    }

    public ArrayList<double[]> getNodes() {
        return nodes;
    }

    public double[] getDistance() {
        return distance;
    }

    public double update(int n1, int n2, int[] newPath){
        updateTotalDistance(n1, n2, newPath);        
        this.path = newPath;
        addTabu(path[n1], n1);
        addTabu(path[n2], n2);

        return getTotalDistance();
    }
    private void updateTotalDistance(int n1, int n2, int[] newPath){
        /**
         * a->(n1)b->c->d->(n2)e->f->g : path
         * a->(n1)b->e->d->(n2)c->f->g : newPath
         * 
         * ny distans fran n1 till n1+1 
         * n1+1 fram till n2(?) ar i omvand ordning
         * n2 till n2+1 ar ny distans.
         * Samma distanser n2+1 till n1.
         * 
         * Tips. Gor en kontroll check med Operations.totalDistance pa newPath
         * 
         */
        int len = nodes.size();
        if(newPath[n1] != path[n1]){
            System.out.println("Avbrottslage 1");
            System.exit(1);
        }
        if(newPath[(n2+1) % len] != path[(n2+1) % len]){
            System.out.println("Avbrottslage 2");
            System.exit(1);
        }

        int i = n1;
        
        // new distance n1->n1+1
        double[] p1 = nodes.get(newPath[i]);
        i = increaseMod(i);
        double[] p2 = nodes.get(newPath[i]);
        double d = Operations.distance(p1, p2);
        totalDistance -= distance[n1];
        distance[n1] = d;
        totalDistance += distance[n1];
        
        // reverse order from n+1 up to n2
        // at start, i = n1+1, j = n2-1
        int j = decreaseMod(n2);
        int stop;
        if(n2 > n1){
            stop = n1 + Math.ceilDiv(n2-n1, 2);
            // System.out.println("stop 1.1: " + stop);

        }
        else{
            stop = n1 + Math.ceilDiv(len+n2-n1, 2);
            stop = Math.floorMod(stop, len);
        }

        // stop = n2 > n1 ? Math.ceilDiv(n2, 2) + n1 : Math.ceilDiv(len + n2, 2) + n1; *****
        if(stop < 0 || stop >= len){
            System.out.println("stop: " + stop);
            System.exit(1);
        }
        // System.out.printf("i at %d, stop at %d\n", i, stop);
        while(i != stop){
            // System.out.printf("swap %d with %d\n", i, j);
            d = distance[i];
            distance[i] = distance[j];
            distance[j] = d;
            i = increaseMod(i);
            j = decreaseMod(j);
        }

        // new distance n2->n2+1
        i = n2;
        p1 = nodes.get(newPath[i]);
        i = increaseMod(i);
        p2 = nodes.get(newPath[i]);
        d = Operations.distance(p1, p2);
        totalDistance -= distance[n2];
        distance[n2] = d;
        totalDistance += distance[n2];

    }

    public void swapPoints(int p1, int p2){
        // 0 1 2 3 4 5 6
        // 0 1 2 3 4 5 6
        // p1    p2

        // swap
        // 3 1 2 0 4 5 6
        int tmp = path[p1];
        path[p1] = path[p2];
        path[p2] = tmp;

        // update distances
        double newDistance = Operations.distance(nodes.get(path[p1]), nodes.get(path[increaseMod(p1)]));
        totalDistance -= distance[p1];
        totalDistance += newDistance;
        distance[p1] = newDistance;

        newDistance = Operations.distance(nodes.get(path[decreaseMod(p1)]), nodes.get(path[p1]));
        totalDistance -= distance[decreaseMod(p1)];
        totalDistance += newDistance;
        distance[decreaseMod(p1)] = newDistance;

        newDistance = Operations.distance(nodes.get(path[p2]), nodes.get(path[increaseMod(p2)]));
        totalDistance -= distance[p2];
        totalDistance += newDistance;
        distance[p2] = newDistance;

        newDistance = Operations.distance(nodes.get(path[decreaseMod(p2)]), nodes.get(path[p2]));
        totalDistance -= distance[decreaseMod(p2)];
        totalDistance += newDistance;
        distance[decreaseMod(p2)] = newDistance;

        // add to tabu-list
        addTabu(path[p1], p1);
        addTabu(path[p2], p2);
    }
    int increaseMod(int i){
        return Math.floorMod((i+1), nodes.size());
    }

    int decreaseMod(int i){
        return Math.floorMod((i-1), nodes.size());
    }
        
}
