/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GraphStuff;

import DataStructures.Graph;
import DataStructures.ParallelizationSupport.ElementProcessor;
import DataStructures.Vertex;
import java.util.Set;

/**
 * At the end of a trimming iteration, populate the set for the next iteration.
 * This class parallelizes that process, as well as decrements the neighborcounts
 * of neighbors of trimmed vertices.
 * @author Benjamin
 */
public class NextIterationSetPopulatorProcessor implements ElementProcessor<Integer>{
    final Graph G;
    final int srchSpaceCol;
    final Set<Integer> nextIterationSet;
    final boolean seek;
    public NextIterationSetPopulatorProcessor(Graph G, int srchSpaceCol, Set<Integer> nextIterationSet, boolean seek){
        this.G = G;
        this.srchSpaceCol = srchSpaceCol;
        this.nextIterationSet = nextIterationSet;
        this.seek = seek;
    }

    Vertex aux;
    @Override
    public void processElement(Integer element) {
        aux = G.vertices[element];
        for(int child: aux.outNeighbours){
            G.vertices[child].nIn.getAndDecrement();
            if(checkColors(child)) nextIterationSet.add(child);
        }
        
        for(int parent: aux.inNeighbours){
            G.vertices[parent].nOut.getAndDecrement();
            if(checkColors(parent)) nextIterationSet.add(parent);
        }
    }
    
    int auxCol;
    private boolean checkColors(int vertexId){
        auxCol = G.vertices[vertexId].color;
        if(seek) return auxCol == srchSpaceCol;
        else return auxCol != srchSpaceCol;
    }
    
    @Override
    public ElementProcessor clone(int cloneId) {
        return new NextIterationSetPopulatorProcessor(G, srchSpaceCol, nextIterationSet, seek);
    }
}
