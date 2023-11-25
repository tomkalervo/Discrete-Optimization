import java.util.ArrayList;
import java.util.LinkedList;

public class Tableu {
    // ArrayList<ArrayList<Value>> nonBasicVariables;
    ArrayList<ArrayList<Value>> decisionVariables;
    ArrayList<Value> quantityVector;
    ArrayList<Integer> basisVector;
    ArrayList<Value> objectiveFun;
    int slackVariable;

    public Tableu(LinkedList<Integer> objectiveFun){
        this.slackVariable = 0;
        this.objectiveFun = new ArrayList<>();
        this.quantityVector = new ArrayList<>();
        this.basisVector = new ArrayList<>();
        this.decisionVariables = new ArrayList<>();

        for (int constant : objectiveFun) {
            this.objectiveFun.add(new Value(constant));
        }
    }

    public void printDual(){
        /**
         * cx1  cx2  cx3   (b)
         * c1x1 c1x2 c1x3  b1 (-> y1)
         * c2x1 c2x2 c3x3  b2 (-> y2)
         * 
         * b1   b2    (c)
         * c1x1 c2x1  cx1
         * c1x2 c2x2  cx2
         * c1x3 c2x3  cx3
         */
        System.out.println("Dual:");
        // System.out.printf("c\t");
        for(int j = 0; j < this.quantityVector.size(); j++)
            System.out.printf("(%d/%d) \t", 
                this.quantityVector.get(j).dividend, 
                this.quantityVector.get(j).divisor);
        System.out.printf("b\n");
        int i = 0;
        while(i < this.objectiveFun.size()){
            // System.out.printf("%d\t", this.basisVector.get(i));
            for(int j = 0; j < this.decisionVariables.size(); j++)
                System.out.printf("(%d/%d) \t", 
                    this.decisionVariables.get(j).get(i).dividend, 
                    this.decisionVariables.get(j).get(i).divisor);

            System.out.printf("(%d/%d)\n", 
                this.objectiveFun.get(i).dividend, 
                this.objectiveFun.get(i).divisor);
            i++;
        }
    }
    public void addEq(LinkedList<Integer> eq, int b){
        ArrayList<Value> newEq = new ArrayList<>();
        for (int constant : eq) {
            newEq.add(new Value(constant));
        }
        for (int i = 0; i < slackVariable; i++){
            decisionVariables.get(i).add(new Value(0));
            newEq.add(new Value(0));
        }
        newEq.add(new Value(1));
        
        decisionVariables.add(newEq);
        quantityVector.add(new Value(b));
        objectiveFun.add(new Value(0));
        basisVector.add(newEq.size()-1);
        slackVariable += 1;
    }

    public int[] getPivotElement(){

        Value c; 
        Value max = null;
        int row = 0;
        int col = 0;
        
        for(int j = 0; j < objectiveFun.size(); j++){
            Value z = new Value(0);
            for(int i = 0; i < decisionVariables.size(); i++){
                Value tmp1 = new Value(0);
                Value tmp2 = new Value(0);
                tmp1.add(decisionVariables.get(i).get(j));
                tmp2.add(objectiveFun.get(basisVector.get(i)));
                tmp1.multiply(tmp2);
                z.add(tmp1);
            }
            c = objectiveFun.get(j);
            // System.out.printf("z_%d is %d/%d and c_%d is %d/%d\n", j, z.dividend, z.divisor, j, c.dividend, c.divisor);
            z.multiply(new Value(-1));
            z.add(c);
            // System.out.printf("c_%d - z_%d = %d/%d\n", j,j,z.dividend,z.divisor);
            if(z.isLarger(max)){
                max = z;
                col = j;
            }
        }
        // System.out.println("Variable to pivot is x_" + col);

        Value minRatio = null;
        for(int i = 0; i < quantityVector.size(); i++){
            // only pick positive variables to enter basis
            if(decisionVariables.get(i).get(col).isPositive()){
                Value ratio = new Value(0);
                ratio.add(quantityVector.get(i));
                ratio.divide(decisionVariables.get(i).get(col));
                // ratio.divide(quantityVector.get(i));
                // System.out.printf("ratio (%d/%d)\n",ratio.dividend,ratio.divisor);
                if(ratio.isSmaller(minRatio)){
                    // System.out.println("smaller");
                    minRatio = ratio;
                    row = i;
                }
            }
            // System.out.printf("minRatio (%d/%d)\n", minRatio.dividend,minRatio.divisor);
        }
        // System.out.println("Variable to leave is x_" + row);
        if(minRatio == null){
            System.err.println("minRatio is null");
            System.exit(1);
        }

        return new int[]{col, row};
    }

    public void pivot(int i, int j){
        basisVector.set(j, i);

        // set pivot-variable to 1
        // and update row
        Value pivotValue = new Value(0);
        pivotValue.add(decisionVariables.get(j).get(i));
        for(int k = 0; k < decisionVariables.get(j).size(); k++){
            decisionVariables.get(j).get(k).divide(pivotValue);
        }
        quantityVector.get(j).divide(pivotValue);

        // set new basis-variable by updating all other rows
        for(int k = 0; k < j; k++){
            pivotValue = new Value(0);
            pivotValue.add(decisionVariables.get(k).get(i));
            for(int l = 0; l < decisionVariables.get(k).size(); l++){
                Value tmpValue = new Value(0);
                tmpValue.add(pivotValue);
                tmpValue.multiply(decisionVariables.get(j).get(l));
                decisionVariables.get(k).get(l).subtract(tmpValue);
            }
            Value tmpValue = new Value(0);
            tmpValue.add(pivotValue);
            tmpValue.multiply(quantityVector.get(j));
            quantityVector.get(k).subtract(tmpValue);
        }

        for(int k = j+1; k < decisionVariables.size(); k++){
            pivotValue = new Value(0);
            pivotValue.add(decisionVariables.get(k).get(i));
            for(int l = 0; l < decisionVariables.get(k).size(); l++){
                Value tmpValue = new Value(0);
                tmpValue.add(pivotValue);
                tmpValue.multiply(decisionVariables.get(j).get(l));
                decisionVariables.get(k).get(l).subtract(tmpValue);
            }
            Value tmpValue = new Value(0);
            tmpValue.add(pivotValue);
            tmpValue.multiply(quantityVector.get(j));
            quantityVector.get(k).subtract(tmpValue);
        }
    }

    public double getSum(){
        double sum = 0;
        for(int j = 0; j < basisVector.size(); j++){
            int x =  basisVector.get(j);
            double cj = quantityVector.get(j).getDouble();
            double xj = objectiveFun.get(x).getDouble();
            sum  +=  objectiveFun.get(x).getDouble() 
                    * quantityVector.get(j).getDouble();
            System.out.printf("c_%d(x_%d) = %.2f(%.2f) \n", x,x, cj, xj);
        }
        return sum;
    }
    public void printAll(){
        System.out.printf("c\t");
        for(int j = 0; j < this.objectiveFun.size(); j++)
            System.out.printf("(%d/%d) \t", 
                this.objectiveFun.get(j).dividend, 
                this.objectiveFun.get(j).divisor);
        System.out.printf("b\n");

        int i = 0;
        while(i < this.decisionVariables.size()){
            System.out.printf("%d\t", this.basisVector.get(i));
            for(int j = 0; j < this.objectiveFun.size(); j++)
                System.out.printf("(%d/%d) \t", 
                    this.decisionVariables.get(i).get(j).dividend, 
                    this.decisionVariables.get(i).get(j).divisor);

            System.out.printf("(%d/%d)\n", this.quantityVector.get(i).dividend, this.quantityVector.get(i).divisor);
            i++;
        }
    }
    public boolean isFeasible(){
        for (Value b : quantityVector) {
            if(b.isNonPositive()){
                if(b.isZero()){
                    System.out.println("B is Zero");
                }
                else{
                    return false;
                }
            }
        }
        return true;
    }
}
