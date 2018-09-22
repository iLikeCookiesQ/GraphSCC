/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GraphStuff.Probability;

/**
 * This class puts vertices in buckets, and then puts those buckets in buckets,
 * and then those buckets in buckets, up to an arbitrary number of times, 
 * depending on the length of the arrays supplied by the main.
 * 
 * The idea is that the first bucketing level clusters the vertices, then the 
 * second level clusters those clusters. The magnitudes array defines the average
 * amount of edges from a cluster to another cluster in the same sub bucket.
 * 
 * @author Benjamin
 */
public class NestedBuckets implements ProbabilityBonusFunction {
    final int bonusType = 0;
    // Lenght defines nesting depth. Each element defines number of buckets at 
    // each level.
    final int[] bucketDefinition;
    final int nestingDepth;
    
    // Desired average amount of edges from an item in a bucket to another item 
    // in that bucket. One value for each bucket level.
    final double[] magnitudes;
    
    final int N; // graph size
    
    // precompute division of next 2 fields for runtime efficiency
    final double oneOverN; 
    final double oneOverNSquared;
    
    public NestedBuckets(int N, int[] bucketDefinition, double[] magnitudes){
        this.bucketDefinition = bucketDefinition;
        this.nestingDepth = this.bucketDefinition.length;
        /*if(nestingDepth == 0){
            System.out.println("NestedBuckets Illegal arguments: array of lenght 0.");
            System.exit(0);
        }//*/
        if(magnitudes.length != nestingDepth){
            System.out.println("NestedBuckets Illegal arguments: arrays of different length.");
            System.exit(0);
        }
        this.magnitudes = magnitudes;
        
        this.N = N;
        this.oneOverN = 1/(double)N;
        this.oneOverNSquared = oneOverN*oneOverN;
        
        // calculate divisions once and then use multiplications for efficiency
        //this.oneOverBucketSize = new double[nestingDepth];
        //oneOverBucketSize[0] = 1.0;
        //for(int i = 1; i < nestingDepth; i++){
        //    oneOverBucketSize[i] = oneOverBucketSize[i-1]/bucketDefinition[i];
        //}
    }
    
    @Override
    public int getBonusType() {
        return bonusType;
    }

    @Override
    public double get2DepBonus(int startVertex, int endVertex) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        int[] startVertexBuckets = new int[nestingDepth];
        int[] endVertexBuckets = new int[nestingDepth];
        
        // assign 1st level buckets
        startVertexBuckets[0] = startVertex%bucketDefinition[0];
        endVertexBuckets[0] = endVertex%bucketDefinition[0];
        
        for(int i = 0; i < nestingDepth; i++){
            if(i != 0){
                // assign subequent level buckets
                startVertexBuckets[i] = startVertexBuckets[i-1]%bucketDefinition[i];
                endVertexBuckets[i] = endVertexBuckets[i-1]%bucketDefinition[i];
            }
            if(startVertexBuckets[i] == endVertexBuckets[i]){
                return getModifiedMagnitude(i);
            } 
        }
        return 0.0;
    }
    
    double getModifiedMagnitude(int k){
        //return magnitudes[currentBucketDepth]*oneOverN*bucketDefinition[currentBucketDepth]*oneOverBucketSize[currentBucketDepth];
        if(k == 0){
            return magnitudes[0]*bucketDefinition[0]*oneOverN;
        } else {
            return magnitudes[k]*bucketDefinition[k]*bucketDefinition[k-1]*oneOverNSquared;
        }
    }

    @Override
    public double getSDepBonus(int startVertex) {throw new UnsupportedOperationException("Not supported.");}
    @Override
    public double getEDepBonus(int endVertex) {throw new UnsupportedOperationException("Not supported.");}
    @Override
    public double getInDepBonus() {throw new UnsupportedOperationException("Not supported.");}
    
}
