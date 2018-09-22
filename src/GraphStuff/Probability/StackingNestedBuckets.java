/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GraphStuff.Probability;

/**
 * This class does nearly the same as NestedBuckets, but vertices already found
 * to be part of the same bucket will continue to receive probability bonuses if 
 * they are also in the same bucket in later bucketing levels.
 * This creates a more smooth curve of the prevalence of SCC sizes.
 * @author Benjamin
 */
public class StackingNestedBuckets extends NestedBuckets {
    public StackingNestedBuckets(int N, int[] bucketDefinition, double[] magnitudes){
        super(N, bucketDefinition, magnitudes);
    }
    
    @Override
    public double get2DepBonus(int startVertex, int endVertex) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        int[] startVertexBuckets = new int[nestingDepth];
        int[] endVertexBuckets = new int[nestingDepth];
        
        // assign 1st level buckets
        startVertexBuckets[0] = startVertex%bucketDefinition[0];
        endVertexBuckets[0] = endVertex%bucketDefinition[0];
        
        double returnValue = 0.0;
        
        for(int i = 0; i < nestingDepth; i++){
            if(i != 0){
                // assign subequent level buckets
                startVertexBuckets[i] = startVertexBuckets[i-1]%bucketDefinition[i];
                endVertexBuckets[i] = endVertexBuckets[i-1]%bucketDefinition[i];
            }
            if(startVertexBuckets[i] == endVertexBuckets[i]){
                returnValue += getModifiedMagnitude(i);
            } else return returnValue;
        }
        return returnValue;
    }
}
