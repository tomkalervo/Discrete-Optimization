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

    //int value = 0;
    //int weight = 0;
    int boundValue;

    public Optimise(boolean maximise, int capacity, int items, int[] values, int[] weights){
        this.maximise = maximise;
        this.capacity = capacity;
        this.items    = items;
        this.values   = values;
        this.weights  = weights;
        this.taken    = new int[items];
        this.boundValue = calcBound();
        System.out.println("Calculated Bound is " + this.boundValue);
    }

    private int calcBound(){
        int value = 0;
        int weight = 0;

        int[] order = sort();
        int k = 0;
        while(k < items && weight < capacity){
            int i = order[k++];
            if(weight + weights[i] <= capacity){
                value += values[i];
                weight += weights[i];
            } else {
                value += (values[i] / weights[i]) * (capacity - weight);
                break;
            }
        }

        return value;
    }

    public int[] getTaken(){
        int weight = 0;
        for(int i=0; i < items; i++){
            if(weight + weights[i] <= capacity){
                taken[i] = 1;
                weight += weights[i];
            } else {
                taken[i] = 0;
            }
        }
        return taken;

    }

    public int getValue(){
        int v = 0;
        for(int i = 0; i < items; i++){
            v += (taken[i] * values[i]);
        }
        return v;
    }

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
            int j = i-1;
            while(j > 0 && relativeValue[i] > relativeValue[j]){
                // swap relative value
                tmp_val = relativeValue[i];
                relativeValue[i] = relativeValue[j];
                relativeValue[j] = tmp_val;
                // swap order
                tmp_order = order[i];
                order[i] = order[j];
                order[j] = tmp_order;
                j--;
            }
        }
        
        return order;
    }



}
