/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GraphStuff;

import DataStructures.Graph;
import DataStructures.ParallelizationSupport.ElementProcessor;
import DataStructures.Vertex;

/**
 * Decrements the neighbor counts of neighbors of a vertex.
 * @author Benjamin
 */
public class NeighborDecrementorProcessor implements ElementProcessor<Integer>{
    
    final Graph G;
    
    public NeighborDecrementorProcessor(Graph G){
        this.G = G;
    }
    
    Vertex aux;
    @Override
    public void processElement(Integer element) {
        aux = G.vertices[element];
        for(int child: aux.outNeighbours) G.vertices[child].nIn.getAndDecrement();
        for(int parent: aux.inNeighbours) G.vertices[parent].nOut.getAndDecrement();
    }
    
    /*
    int auxCol;
    private boolean checkColors(int vertexId){
        auxCol = G.vertices[vertexId].color;
        if(seek) return (auxCol == fwdCol || auxCol == bwdCol);
        else return auxCol != fwdCol;
    }//*/

    @Override
    public ElementProcessor<Integer> clone(int cloneId) {
        return new NeighborDecrementorProcessor(G);
    }
}
