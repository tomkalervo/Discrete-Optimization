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
        LinkedList<Double> obj = new LinkedList<>();
        obj.add(1.1);
        obj.add(1.0);
        Tableu t = new Tableu();
        t.setObjectiveFun(obj);

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
        System.out.println("------------------");
        int[] pivot = t.getPivotElement();
        t.pivot(pivot[0], pivot[1]);
        while(!t.isOptimal()){
            t.printAll();
            System.out.println("------------------");

            pivot = t.getPivotElement();
            t.pivot(pivot[0], pivot[1]);
            if(!t.isFeasible())
                break;
        }
        t.printAll();
        System.out.println("------------------");

        t.printDual();

        System.out.println(t.getSum());
    } 

    public static void main(String[] args){
        testTableu();
    }
}
