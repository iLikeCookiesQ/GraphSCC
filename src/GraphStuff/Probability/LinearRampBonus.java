/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GraphStuff.Probability;

import GraphStuff.FWBW.FWBW;

/**
 * Implements a linearly increasing bonus based on vertexId.
 * The lowest vertexId gets a bonus of 0, and the highest gets
 * a bonus of + 1*magnitude/N. This puts the average bonus at 0.5*magnitude/N.
 * 
 * The average vertex thus has 0.5*magnitude edges as a result of this bonus.
 * @author Benjamin
 */
public class LinearRampBonus implements ProbabilityBonusFunction{
    final int bonusType;
    double magnitude;
    
    int N;
    final double oneOverN;
    
    public LinearRampBonus(int N, double magnitude, int bonusType){
        
        this.magnitude = magnitude;
        this.bonusType = bonusType;
        
        this.N = N;
        this.oneOverN = 1/(double)N;
    }

    @Override
    public int getBonusType() {
        return bonusType;
    }

    @Override
    public double getSDepBonus(int startVertex) {
        return calculate(startVertex);
    }

    @Override
    public double getEDepBonus(int endVertex) {
        return calculate(endVertex);
    }
    
    private double calculate(int vertexId) {
        // make 0 <= x < 1 with the next 2 statements
        double x = vertexId*oneOverN;
        
        return magnitude*x*oneOverN;
    }   

    /**
     * In this mode, the bonus for an edge depends on whichever vertex (start or end)
     * has the highest vertex identifier. This will give the vertices with a high
     * vertex identifier both increased chance for out-neighbors as well as increased
     * chance for in-neighbors.
     */
    @Override
    public double get2DepBonus(int startVertex, int endVertex) {
        if(startVertex > endVertex) return calculate(startVertex);
        else return calculate(endVertex);
    }
    
    @Override
    public double getInDepBonus() {throw new UnsupportedOperationException("Not supported");}
}
