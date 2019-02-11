/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GraphStuff.Probability;

/**
 * Same as StackingNestedBuckets, except the magnitude of the probability bonuses are scaled based on the identifier of the 
 * equivalence class that two vertices that are in the same class are in.
 * 
 * Example: two vertices that both have their equivalence class on 0, they will receive a 1/nBuckets multiplier.
 * This ramps linearly to a multiplier of 1 when the equivalence class = nBuckets.
 * @author Benjamin
 */
public class LinearStackingNestedBuckets extends StackingNestedBuckets{

    public LinearStackingNestedBuckets(int N, int[] bucketDefinition, double[] magnitudes) {
        super(N, bucketDefinition, magnitudes);
    }
    
    @Override
    double getEquivalenceClassModifier(int a, int b){
        //return (equivalenceClass+1)*oneOverNBuckets[bucketingLevel];
        
        //
        int highest;
        if(a > b) highest = a;
        else highest = b;
        return highest*1.0/N;//*/
    }
}
