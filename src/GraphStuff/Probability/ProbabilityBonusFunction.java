/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GraphStuff.Probability;

/**
 * This interface lets you define different probability bonuses for edges to
 * exist during random graph generation.
 * @author Benjamin
 */
public interface ProbabilityBonusFunction {
    /** 
     * defines the probability bonus for the chance for the edge from 
     * startVertex to endVertex to exist.
     * 
     * If you want to use a function that only takes 1 argument, use startVertex
     * as the vertex identifier.
     */
    
    /**
     * Defines the type of bonus.
     * 0 : regular doubly dependent bonus.
     * 1 : Only dependent on starting Vertex.
     * 2 : Only dependent on ending Vertex
     * 3 : Independent on vertex id.
     * @return 
     */
    int getBonusType();
    
    double get2DepBonus(int startVertex, int endVertex);
    double getSDepBonus(int startVertex);
    double getEDepBonus(int endVertex);
    double getInDepBonus();
}
