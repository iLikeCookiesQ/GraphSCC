/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GraphStuff;

import DataStructures.Graph;
import DataStructures.ParallelizationSupport.ElementProcessor;


/**
 * Recounts the neighbors with color setCol of a vertex.
 * @author Benjamin
 */
public class NeighborRecounterProcessor implements ElementProcessor<Integer> {
    final int setCol;
    final Graph G;
    final boolean seek; // seek color if true, avoid color if false.
    
    public NeighborRecounterProcessor(int setCol, Graph G, boolean seek){
        this.setCol = setCol;
        this.G = G;
        this.seek = seek;
    }
    
    /*
    private NeighborRecounterProcessor(int setCol, Graph G, boolean seek, int threadId){
        //super(threadId);
        this.setCol = setCol;
        this.G = G;
        this.seek = seek;
    }//*/
    
    int auxVertex, nOut, nIn;
    @Override
    public void processElement(Integer element) {
        nOut = 0;
        nIn = 0;
        //for(int j = 0; j < G.vertices[element].outNeighbours.length; j++){
        for(int child: G.vertices[element].outNeighbours){
            if(checkColors(child)) nOut++;
        }
        //for(int j = 0; j < G.vertices[element].inNeighbours.length; j++){
        for(int parent: G.vertices[element].inNeighbours){
            if(checkColors(parent)) nIn++;
        }
        G.vertices[element].nOut.set(nOut);
        G.vertices[element].nIn.set(nIn);
    }   
    
    private boolean checkColors(int vertexId){
        if(seek) return G.vertices[vertexId].color == setCol;
        else return G.vertices[vertexId].color != setCol;
    }

    @Override
    public ElementProcessor<Integer> clone(int cloneId) {
        return new NeighborRecounterProcessor(setCol, G, seek);
    }
}
