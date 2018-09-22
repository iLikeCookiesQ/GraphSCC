/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GraphStuff.Probability;

import GraphStuff.FWBW.FWBW;

/**
 * This class assigns vertices to buckets. Vertices in buckets have increased
 * probability to have edges to each other.
 * 
 * The probability bonus is such that on average, the number of edges that a
 * vertex has to other vertices in the same bucket is equal to magnitude.
 * @author Benjamin
 */
public class BucketBonus implements ProbabilityBonusFunction{
    final int N;
    final double effectiveMagnitude; // The bonus in multiples of 1/N.
    final int nBuckets;
    
    final int bonusType = 0;
    public BucketBonus(int N, double magnitude, int nBuckets){
        this.N = N;
        this.nBuckets = nBuckets;
        this.effectiveMagnitude = magnitude*nBuckets/(double)N;
        
    }
    
    @Override
    public int getBonusType() {
        return bonusType;
    }

    @Override
    public double get2DepBonus(int startVertex, int endVertex) {
        if(startVertex%nBuckets == endVertex%nBuckets) return effectiveMagnitude;
        else return 0;
    }

    @Override
    public double getSDepBonus(int startVertex) {throw new UnsupportedOperationException("Not supported.");}
    @Override
    public double getEDepBonus(int endVertex) {throw new UnsupportedOperationException("Not supported.");}
    @Override
    public double getInDepBonus() {throw new UnsupportedOperationException("Not supported.");}
    
}
