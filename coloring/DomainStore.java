import java.util.ArrayList;
import java.util.LinkedList;
import java.util.stream.IntStream;

public class DomainStore {
        ArrayList<LinkedList<Integer>> domainList;
        int usedValues;
        int variable = 0;
        boolean solved = false;

        /**
         * 
         * @param domainList Contains the current domainList of values related to variables
         * @param usedValues The sum of used/assigned values
         */
        public DomainStore(ArrayList<LinkedList<Integer>> domainList, int usedValues){
            this.domainList = new ArrayList<LinkedList<Integer>>(domainList);
            this.usedValues = usedValues;
        }

        /**
         * Initialize a DomainStore object with a new Domain List 
         * @param domainSize Sets the size of the Domain list as well as the (increasing) length of each domain
         */
        public DomainStore(int domainSize){
            // Build DomainList
            this.domainList = new ArrayList<LinkedList<Integer>>();
            for(int i = 0; i < domainSize; i++){
                domainList.add(null);
            }
            for(int i = 0; i < domainSize; i++){
                domainList.set(i, 
                    new LinkedList<Integer>(
                    IntStream
                    .range(0, i+1)
                    .boxed()
                    .toList()));
            }
            // Init
            this.usedValues = 0;
        }

        public DomainStore(int domainSize, int maxValue){
            // Build DomainList
            this.domainList = new ArrayList<LinkedList<Integer>>();
            for(int i = 0; i < domainSize; i++){
                domainList.add(null);
            }
            for(int i = 0; i < domainSize; i++){
                domainList.set(i, 
                    new LinkedList<Integer>(
                    IntStream
                    .range(0, maxValue+1)
                    .boxed()
                    .toList()));
            }
            // Init
            this.usedValues = 0;
        }

        public DomainStore(int domainSize, int maxValue, int[] order){
            // Build DomainList
            this.domainList = new ArrayList<LinkedList<Integer>>();
            for(int i = 0; i < domainSize; i++){
                domainList.add(null);
            }
            for(int i = 0; i < domainSize; i++){
                domainList.set(order[i], 
                    new LinkedList<Integer>(
                    IntStream
                    .range(0, maxValue+1)
                    .boxed()
                    .toList()));
            }
            // Init
            this.usedValues = 0;
        }

        public DomainStore(int domainSize, int startValue, int maxValue){
            // Build DomainList
            this.domainList = new ArrayList<LinkedList<Integer>>();
            for(int i = 0; i < domainSize; i++){
                domainList.add(null);
            }
            maxValue++;
            for(int i = 0; i < domainSize; i++){
                domainList.set(i, 
                    new LinkedList<Integer>(
                    IntStream
                    .range(0, maxValue + 1)
                    .boxed()
                    .toList()));

            }
            for(int i = 0; i < maxValue; i++){
                domainList.set(i, 
                    new LinkedList<Integer>(
                    IntStream
                    .range(0, i + 1)
                    .boxed()
                    .toList()));
            }
            // Init
            this.usedValues = 0;
        }

        public DomainStore(DomainStore ds){
            this.domainList = ds.getdomainList();
            this.usedValues = ds.getUsedValues();
            this.solved = ds.solved();
            this.variable = ds.variable;
        }

        public int size(){
            return this.domainList.size();
        }
        public int getUsedValues(){
            int value = 0;
            for (LinkedList<Integer> value_list : domainList) {
                if(value_list.size() == 1)
                    value = value_list.getFirst() > value ? value_list.getFirst() : value;
            }

            return value;
        }

        public int getNextVariable(){
            if(this.variable >= this.size())
                return this.variable;

            while(this.domainList.get(this.variable).size() == 1 && ++this.variable < this.size());
            
            return this.variable;
        }

        public void setVariable(int v) {
            this.variable = v < this.size() ? v : 0;
        }

        public ArrayList<LinkedList<Integer>> getdomainList(){
            ArrayList<LinkedList<Integer>> tmpDL = new ArrayList<LinkedList<Integer>>(this.domainList.size());
            for(int i = 0; i < this.domainList.size(); i++){
                tmpDL.add(i, new LinkedList<>(this.domainList.get(i)));
            }
            return tmpDL;
        }

        public void setdomainList(ArrayList<LinkedList<Integer>> domainList){
            this.domainList = domainList;
        }

        public void addValueToDomains(int value){
            for (LinkedList<Integer> domain : this.domainList) {
                domain.addLast(value);
            }
        }

        public void addValueToUndecidedDomains(int value){
            for (LinkedList<Integer> domain : this.domainList) {
                if(domain.size() > 1)
                    domain.addLast(value);
            }
        }

        public boolean solved(){
            if(this.solved)
                return true;
            else{
                for (LinkedList<Integer> variable : this.domainList) {
                    if(variable.size() > 1)
                        return false;
                }
                return true;
            }

        }

        public boolean removeValue(int variable, int value) {
            return this.domainList.get(variable).removeFirstOccurrence(value);
        }

        public int decidedVariables(){
            int n = 0;
            for (LinkedList<Integer> linkedList : domainList) {
                if(linkedList.size() == 1)
                    n++;
            }

            return n;
        }



}
