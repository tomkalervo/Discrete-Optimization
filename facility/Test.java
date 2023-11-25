import java.util.LinkedList;

public class Test {
    private static void testValues(){
        Value v1 = new Value(1);
        Value v2 = new Value(1);
        System.out.printf("v1 {%d / %d}, v2 {%d / %d}\n", v1.dividend, v1.divisor, v2.dividend, v2.divisor);
        v1.add(v2);
        System.out.println("v1 + v2");
        System.out.printf("v1 {%d / %d}, v2 {%d / %d}\n", v1.dividend, v1.divisor, v2.dividend, v2.divisor);
        v2.add(v1);
        System.out.println("v2 + v1");
        System.out.printf("v1 {%d / %d}, v2 {%d / %d}\n", v1.dividend, v1.divisor, v2.dividend, v2.divisor);
        v1.add(v2);
        System.out.println("v1 + v2");
        System.out.printf("v1 {%d / %d}, v2 {%d / %d}\n", v1.dividend, v1.divisor, v2.dividend, v2.divisor);
        v1.divide(v2);
        System.out.println("v1 / v2");
        System.out.printf("v1 {%d / %d}, v2 {%d / %d}\n", v1.dividend, v1.divisor, v2.dividend, v2.divisor);
        v2.multiply(v1);
        System.out.println("v2 * v1");
        System.out.printf("v1 {%d / %d}, v2 {%d / %d}\n", v1.dividend, v1.divisor, v2.dividend, v2.divisor);
        v1.subtract(v1);
        System.out.println("v1 - v2");
        System.out.printf("v1 {%d / %d}, v2 {%d / %d}\n", v1.dividend, v1.divisor, v2.dividend, v2.divisor);
        v1.divide(v2);
        System.out.println("v1 / v2");
        System.out.printf("v1 {%d / %d}, v2 {%d / %d}\n", v1.dividend, v1.divisor, v2.dividend, v2.divisor);
        v1.subtract(v2);
        System.out.println("v1 - v2");
        System.out.printf("v1 {%d / %d}, v2 {%d / %d}\n", v1.dividend, v1.divisor, v2.dividend, v2.divisor);
        v1.divide(v2);
        System.out.println("v1 / v2");
        System.out.printf("v1 {%d / %d}, v2 {%d / %d}\n", v1.dividend, v1.divisor, v2.dividend, v2.divisor);
        v2.add(v1);
        System.out.println("v2 + v1");
        System.out.printf("v1 {%d / %d}, v2 {%d / %d}\n", v1.dividend, v1.divisor, v2.dividend, v2.divisor);
       
        v2.add(v1);
        System.out.println("v2 + v1");
        System.out.printf("v1 {%d / %d}, v2 {%d / %d}\n", v1.dividend, v1.divisor, v2.dividend, v2.divisor);
       
        v2.add(v1);
        System.out.println("v2 + v1");
        System.out.printf("v1 {%d / %d}, v2 {%d / %d}\n", v1.dividend, v1.divisor, v2.dividend, v2.divisor);
       
        v1.subtract(v2);
        System.out.println("v1 - v2");
        System.out.printf("v1 {%d / %d}, v2 {%d / %d}\n", v1.dividend, v1.divisor, v2.dividend, v2.divisor);
       
        v1.divide(v2);
        System.out.println("v1 / v2");
        System.out.printf("v1 {%d / %d}, v2 {%d / %d}\n", v1.dividend, v1.divisor, v2.dividend, v2.divisor);
       
        v2.subtract(v1);
        System.out.println("v2 - v1");
        System.out.printf("v1 {%d / %d}, v2 {%d / %d}\n", v1.dividend, v1.divisor, v2.dividend, v2.divisor);
       
    }
    private static void testTableu(){
        // max 7x_1 + 6x_2
        // eq_1 2x_1 + 4x_2 + s_1 = 16
        // eq_2 3x_1 + 2x_2 + s_2 = 12
        LinkedList<Integer> obj = new LinkedList<>();
        obj.add(1);
        obj.add(1);
        Tableu t = new Tableu(obj);

        // LinkedList<Integer> eq1 = new LinkedList<>();
        // eq1.add(1);
        // eq1.add(0);
        // t.addEq(eq1, 2);

        LinkedList<Integer> eq2 = new LinkedList<>();
        eq2.add(0);
        eq2.add(1);
        t.addEq(eq2, 1);

        LinkedList<Integer> eq3 = new LinkedList<>();
        eq3.add(3);
        eq3.add(-1);
        t.addEq(eq3, 4);

        LinkedList<Integer> eq4 = new LinkedList<>();
        eq4.add(-1);
        eq4.add(5);
        t.addEq(eq4, 4);
        
        t.printAll();
        int[] pivot = t.getPivotElement();
        t.pivot(pivot[0], pivot[1]);
        while(!isOptimal(t)){
            t.printAll();
            pivot = t.getPivotElement();
            t.pivot(pivot[0], pivot[1]);
            if(!t.isFeasible())
                break;
        }
        t.printAll();
        t.printDual();
        System.out.println(isOptimal(t));
        System.out.println(t.getSum());
    } 
    public static boolean isOptimal(Tableu t){
        for(int j = 0; j < t.objectiveFun.size(); j++){
            Value zValue = new Value(0);
            for(int i = 0; i < t.basisVector.size(); i++){
                Value tmpValue = new Value(0);
                tmpValue.add(t.objectiveFun.get(t.basisVector.get(i)));
                tmpValue.multiply(t.decisionVariables.get(i).get(j));
                zValue.add(tmpValue);
            }
            // System.out.printf("zValue_%d (%d/%d)\n", j, zValue.dividend, zValue.divisor);
            zValue.multiply(new Value(-1));
            zValue.add(t.objectiveFun.get(j));
            // System.out.printf("c_%d - z_%d (%d/%d)\n", j, j, zValue.dividend, zValue.divisor);
            if(zValue.isPositive())
                return false;
        }
        return true;
    }
    public static void main(String[] args){
        testTableu();
    }
}
