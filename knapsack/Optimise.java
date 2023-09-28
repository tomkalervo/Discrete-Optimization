import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

public class Optimise {
    boolean maximise;  // true if max, false if min
    int items;
    int capacity;
    int[] values;
    int[] weights;
    int[] taken;
    int[] linearRelaxOrder;

    // BnB
    int bestValue = 0;
    // linear relaxation
    int boundValue;
    // Optimal solution
    boolean optimal = false;

    // start time, used to abort if operations take to long
    long startTime;
    long abortTimeNanoSeconds = 4500000000L;

    public Optimise(boolean maximise, int capacity, int items, int[] values, int[] weights){
        this.maximise = maximise;
        this.capacity = capacity;
        this.items    = items;
        this.values   = values;
        this.weights  = weights;
        this.taken    = new int[items];
        this.linearRelaxOrder = sort();
        this.boundValue = calcBound();
        this.bestValue = greedy();
        this.startTime = System.nanoTime();
    }

    private int calcBound(){
        int value = 0;
        int weight = 0;

        int[] order = this.linearRelaxOrder;
        int k = 0;
        while(k < items && weight < capacity){
            int i = order[k++];
            if(weight + weights[i] <= capacity){
                value += values[i];
                weight += weights[i];
            } 
            else {
                double fraction = (double)values[i] / weights[i];
                value += (int) Math.ceil(fraction * (capacity - weight));
                break;
            }
        }

        return value;
    }
    private int greedy(){
        int weight = 0;
        int value = 0;

        for(int i=0; i < items; i++){
            int o = linearRelaxOrder[i];
            if(weight + weights[o] <= capacity){
                taken[o] = 1;
                weight += weights[o];
                value  += values[o];
            } else {
                taken[o] = 0;
            }
        }
        return value;
    }

    public int[] getTaken(){
        return this.taken;
    }

    public int getValue(){
        return this.bestValue;
    }

    public boolean getOptimal(){
        return this.optimal;
    }

    /**
     * Sort by value/weight ratio in descending order.
     * The sorted order is stored in an integer array
     * @return int[] order 
     */
    private int[] sort(){
        int[] order = new int[items];
        for(int i = 0; i < items; i++)
            order[i] = i;
        double[] relativeValue = new double[items];
        for(int i = 0; i < items; i++){
            relativeValue[i] = (double)values[i]/weights[i]; 
        }

        // simple insertion sort
        double tmp_val; 
        int tmp_order;

        for(int i = 1; i < items; i++){
            tmp_val = relativeValue[i];
            tmp_order = order[i];
            int j = i-1;
            // descending order
            while(j >= 0 && relativeValue[j] < tmp_val){
                // move relative value
                relativeValue[j+1] = relativeValue[j];
                // move order
                order[j+1] = order[j];
                j--;
            }
            relativeValue[j+1] = tmp_val;
            order[j+1] = tmp_order;
        }
        
        return order;
    }

    public void branchAndBound(){
        int remValue = 0;
        for (int v : values) {
            remValue += v;
        }
        this.optimal = branchAndBound(0, 0, this.capacity, remValue, new int[items]);
    }

    private boolean branchAndBound(int value, int i, int remCapacity, int remValue, int[] tmpTaken){
        if(i == items){
            if(value > bestValue){
                this.bestValue = value;
                for(int k = 0; k < items; k++)
                    this.taken[k] = tmpTaken[k];

                //printInfo();
            }
            return true;
        }
        if(remValue + value < this.bestValue)
            return false;


        if(remCapacity - weights[i] >= 0){
            // time check abort
            if(System.nanoTime() - startTime > abortTimeNanoSeconds)
                return false;
            // recursion w/ item
            tmpTaken[i] = 1;
            branchAndBound(value+values[i], i+1, remCapacity - weights[i], remValue, tmpTaken);
        }
        // time check abort
        if(System.nanoTime() - startTime > abortTimeNanoSeconds)
            return false;
        // recursion w/ out item
        tmpTaken[i] = 0;
        branchAndBound(value, i+1, remCapacity, remValue - values[i], tmpTaken);

        // return true if exhaustive search is complete, else false
        if(System.nanoTime() - startTime > abortTimeNanoSeconds)
            return false;
        else
            return true;
    }

    private void printInfo(){
        int v = 0;
        for(int i = 0; i < items; i++)
            v += this.taken[i] * this.values[i];

        System.out.printf("Best value is %d, but calculated current taken items give %d\n", this.bestValue, v);
        for(int i=0; i < items; i++){
            System.out.print(taken[i]+" ");
        }
        System.out.println("");   
    }
}
