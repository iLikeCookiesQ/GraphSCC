/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GraphStuff.MultiStep;

import DataStructures.Graph;
import DataStructures.ParallelizationSupport.ElementProcessor;

/**
 * Not in use.
 * 
 * Callback class for setting vertex color equal to their vertex identifier.
 * @author Benjamin
 */
public class ColorInitializationProcessor implements ElementProcessor<Integer>{

    final Graph G;
    
    ColorInitializationProcessor(Graph G){
        //super(0);
        this.G = G;
    }
    
    /*
    private ColorInitializationProcessor(Graph G, int threadId){
        //super(threadId);
        this.G = G;
    }//*/
    
    @Override
    public void processElement(Integer element) {
        G.vertices[element].color = element;
    }

    @Override
    public ElementProcessor<Integer> clone(int cloneId) {
        return new ColorInitializationProcessor(G);
    }
    
}
