/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GraphStuff.Probability;

import GraphStuff.FWBW.FWBW;
import java.util.Random;

/**
 * Assigns a normally distributed random bonus.
 * The number magnitude is normalized against N.
 * The average probability bonus is magnitude/N while using this class,
 * 
 * @author Benjamin
 */
public class NormallyDistributedBonus implements ProbabilityBonusFunction{
    final int bonusType = 3;
    double magnitude;
    
    Random random;
    
    // Multiplies Math.abs(nextGaussian()) to make an average of 1.
    final double normalizer;
    // Calculates 1/N once, and not many times at runtime.
    final int N;
    final double oneOverN;
    
    public NormallyDistributedBonus(int N, double magnitude){
        this.magnitude = magnitude;
        
        random  = new Random(System.currentTimeMillis());
        normalizer = 2*Math.sqrt(Math.PI);
        this.N = N;
        oneOverN = 1/(double)N;
    }

    @Override
    public int getBonusType() {
        return bonusType;
    }

    @Override
    public double getInDepBonus() {
        return magnitude*normalizer*Math.abs(random.nextGaussian())*oneOverN;
    }
    
    @Override
    public double get2DepBonus(int startVertex, int endVertex) {throw new UnsupportedOperationException("Not supported");}
    @Override
    public double getSDepBonus(int startVertex) {throw new UnsupportedOperationException("Not supported");}
    @Override
    public double getEDepBonus(int endVertex) {throw new UnsupportedOperationException("Not supported");}
}
