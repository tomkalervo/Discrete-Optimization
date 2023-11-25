public class Value{
        int dividend;
        int divisor;
        public Value(int integer){
            this.dividend = integer;
            this.divisor = 1;
        }
        public Value(int dividend, int divisor){
            this.dividend = dividend;
            this.divisor = divisor;
        }
        public boolean isInteger(){
            return divisor == 1;
        }
        public boolean isLessThanOne(){
            return divisor > dividend;
        }
        public int getValueInteger(){
            return dividend;
        }
        public double getValueDouble(){
            return (double)dividend/divisor;
        }
        public void add(Value a){
            // {1/2} + {3/2} 
            int tmpDividend = divisor * a.dividend; // 6 = 2 * 3
            dividend = dividend * a.divisor;        // 2 = 1 * 2
            divisor  = divisor * a.divisor;         // 4 = 2 * 2
            // {2/4} + {3/2}
            dividend = tmpDividend + dividend;      // 8 = 6 + 2
            // {8/4}
            this.minimiseFraction();
        }
        public void subtract(Value a){
            // {1/2} - {3/2} 
            int tmpDividend = divisor * a.dividend; // 6 = 2 * 3
            dividend = dividend * a.divisor;        // 2 = 1 * 2
            divisor  = divisor * a.divisor;         // 4 = 2 * 2
            // {2/4} - {3/2}
            dividend = dividend - tmpDividend;      // -4 = 2 - 4
            // {4/4}
            this.minimiseFraction();
        }
        public void multiply(Value a){
            if(a.dividend == 0){
                this.dividend = 0;
                this.divisor = 1;
                return;
            }

            int i = a.dividend < 0 ? (a.dividend * -1) : a.dividend;
            int v = dividend;
            while(i > 1){
                dividend += v;
                i -= 1;
            }
            divisor = divisor * a.divisor;
            this.minimiseFraction();
            dividend = a.dividend < 0 ? (dividend * -1) : dividend;

        }
        public void divide(Value a){
            if(a.dividend == 0){
                System.err.println("Division by zero");
                System.exit(1);
            }
            Value tmpValue = new Value(a.divisor, a.dividend);
            if(a.dividend < 0){
                tmpValue.dividend = tmpValue.dividend * -1;
                tmpValue.divisor  = tmpValue.divisor * -1;
            }
            this.multiply(tmpValue);
        }

        private void minimiseFraction() {
            if(dividend == 0){
                divisor = 1;
                return;
            }
            // System.out.printf("b: dividend: %d, divisor: %d \n", dividend, divisor); 

            int gcd;
            if(dividend < 0)
                gcd = getGCD(0-dividend, divisor);
            else
                gcd = getGCD(dividend, divisor);

            dividend = dividend / gcd;
            divisor = divisor / gcd;
        }
        private static int getGCD(int dividend, int divisor) {
            int a,b,rem;

            if(dividend > divisor){
                a = dividend;
                b = divisor;
            }
            else{
                a = divisor;
                b = dividend;
            }
            // System.out.printf("a: dividend: %d, divisor: %d \n", dividend, divisor); 

            rem = a % b;
            while(rem != 0){
                a = b;  
                b = rem;  
                if(b < 0){
                    System.out.printf("a: %d, b: %d \n", a, b); 
                    System.exit(1);

                }
                rem = a % b;
            }

            return b;
        }

        public boolean isLarger(Value max) {
            if(max == null)
                return true;

            Value tmp = new Value(0);
            tmp.add(max);
            tmp.subtract(this);
            if(tmp.dividend < 0)
                return true;
            else
                return false;
        }
        public boolean isPositive() {
            return this.dividend > 0;
        }
        public boolean isNonPositive() {
            return !(this.dividend > 0);
        }
        public boolean isZero(){
            return this.dividend == 0;
        }
        public boolean isSmaller(Value min) {
            if(min == null)
                return true;

            Value tmp = new Value(0);
            tmp.add(min);
            tmp.subtract(this);
            if(tmp.dividend > 0)
                return true;
            else
                return false;
        }
        public double getDouble() {
            return (double)dividend/divisor;
        }
    }

