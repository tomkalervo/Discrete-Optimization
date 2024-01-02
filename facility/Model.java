// Work in progress.
public class Model {
    private int nrOfWarehouses, nrOfCustomers;
    private int[]      sVector; // fixed cost of set-up
    private int[]      dVector; // fixed demand of customer
    private int[]      capVector; // fixed cost of set-up
    private double[][] xPosVector; // Position of warehouse in Euclidian space
    private double[][] yPosVector; // Position of customer in Euclidian space
    private int[]      xVector; // decision-variable of warehouses
    private int[][]    yMatrix; // decision-variable of customers served by warehouses
    private double     optimalR;

    public Model(int n, int m){
        this.nrOfWarehouses = n;
        this.nrOfCustomers = m;
        this.sVector = new int[n];
        this.capVector = new int[n];
        this.dVector = new int[m];
        this.xPosVector = new double[n][2];
        this.yPosVector = new double[m][2];
        this.xVector = new int[n];
        this.yMatrix = new int[n][m];
    }
    public void insertWarehouse(int w, int s, int cap, double p1, double p2){
        this.sVector[w] = s;
        this.capVector[w] = cap;
        this.xPosVector[w][0] = p1;
        this.xPosVector[w][1] = p2;
    }
    public void insertCustomer(int c, int d, double p1, double p2){
        this.dVector[c] = d;
        this.yPosVector[c][0] = p1;
        this.yPosVector[c][1] = p2;
    }

    public boolean legalSolution(){
        // constraints
        // 1. Warehouse can only serve client if open
        for(int w = 0; w < nrOfWarehouses; w++){
            for(int c = 0; c < nrOfCustomers; c++){
                if(yMatrix[w][c] > xVector[w])
                return false;
            }
        }
        // 2. Customers must be served by exactly one warehouse
        for(int c = 0; c < nrOfCustomers; c++){
            int sumC = 0;
            for(int w = 0; w < nrOfWarehouses; w++){
                sumC += yMatrix[w][c];
            }
            if(sumC != 1)
                return false;
        }

        return true;
    }
    /**
     * DECISIONVARIABLES
     * X_(w,c) and Y_w
     * X relates customer to warehouse, Y tells if warehouse is open.
     * 
     * CONSTRAINTS
     * a customer c is assigned to exactly one warehouse
     * sum for w=1,...,n [X_(w,c)]     == 1,           for all c
     * Total demand of customers assigned to warehouse w do not exceed capacity of warehouse w
     * sum for c=1,...,m [X_(w,c) * c] <= capacity(w), for all w
     * 
     * OBJECTIVE FUN
     * min: 
     *       for w=1,...,n sum[ for c=1,...,m sum[X_(w,c)] * setupCost_w ]
     *       + for w=1,...,n c=1,...,m sum[X_(w,c) * distance(X_(w,c))]
     * 
     * we have (w * c) x variables and (w) y variables that belong to {0,1}
     * x_w1_c1 x_w1_c2 x_w1_c3 y_w1 bx_11 bx_12 bx_13 by_1
     * x_w2_c1 x_w2_c2 x_w2_c3 y_w2 bx_21 bx_22 bx_23 by_2
     * x_w3_c1 x_w3_c2 x_w3_c3 y_w3 bx_31 bx_32 bx_33 by_3
     * 
     * ASSIGNMENT MATRIX customer - facility
     *   0  1  2  constraint    Linear relaxation
     * 0[1][0][0] = 1           (0 <= x <= 1)
     * 1[1][0][0] = 1           (0 <= x <= 1)
     * 3[0][0][1] = 1           (0 <= x <= 1)
     * 4[1][0][0] = 1           (0 <= x <= 1)
     * 5[0][0][1] = 1           (0 <= x <= 1)
     * 
     * sum(x_w_c, for all c) >= 1, for all w
     * 
     * 
     * FIXED COST  SET-UP facility
     * 0[1]
     * 1[0] X [c_0][c_1][c_3]
     * 2[1]
     * 
     * setCost_w >= 0, for all w
     * 
     * 
     * DEMAND - CAPACITY (c*w size of matrix)
     * (- d_w_c) + cap_w >= 0  for all c, for all w
     */
    
}
