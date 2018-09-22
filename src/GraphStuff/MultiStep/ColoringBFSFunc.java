/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GraphStuff.MultiStep;

import GraphStuff.FWBW.FWBW;
import DataStructures.Graph;
import DataStructures.SplittableSinglyLinkedQueue;
import DataStructures.Vertex;
import GraphStuff.BFSFunctionInterface;
import java.util.Set;
//import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This class does exactly the same as SimpleBWCallback and can thus be removed.
 * It turned out to be exactly the same after it was reworked.
 * 
 * This class implements the BFSFunction that is used in the backwards BFS of
 * the coloring method.
 * 
 * This class is no longer called by ColoringSecondPhaseRunnable
 * @author Benjamin
 */
public class ColoringBFSFunc implements BFSFunctionInterface{
    
    final SplittableSinglyLinkedQueue<Integer> queue;
    final Set<Integer> sccSet;
    final int srchSpaceCol;
    final int sccCol;
    //final AtomicBoolean[] visited;
    //final SplittableSinglyLinkedQueue<Integer> visitedList;
    //final AtomicInteger matchCounter;
    
    final Graph G;
    
    ColoringBFSFunc(
            SplittableSinglyLinkedQueue<Integer> queue,
            Set<Integer> sccSet,
            int srchSpaceCol,
            int sccCol
            //,AtomicBoolean[] visited,
            //SplittableSinglyLinkedQueue<Integer> visitedList
            //,AtomicInteger matchCounter
    ){
        
        this.queue = queue;
        this.sccSet = sccSet;
        this.srchSpaceCol = srchSpaceCol;
        this.sccCol = sccCol;
        //this.visited = visited;
        //this.visitedList = visitedList;
        //this.matchCounter = matchCounter;
        
        this.G = FWBW.G;
    }

    @Override
    public void process(int currentElem) {
        //int parentCol;
        Vertex currentVertex, parentVertex;
        
        currentVertex = G.vertices[currentElem];
        for(int parent: currentVertex.inNeighbours){
            //if(visited[parent].getAndSet(true)) continue;  
            //visitedList.enq(parent);
            
            parentVertex = G.vertices[parent];
            //parentCol = parentVertex.color;
            if(parentVertex.color == srchSpaceCol){
                parentVertex.color = sccCol;
                //matchCounter.getAndIncrement();
                sccSet.add(parent);
                queue.enq(parent);
                MultiStep.remainingVertices.remove(parent);
            }
        }
    }

    @Override
    public BFSFunctionInterface createCopy(SplittableSinglyLinkedQueue<Integer> q) {
        return new ColoringBFSFunc(q, sccSet, srchSpaceCol, sccCol
                //,visited, visitedList
                //,matchCounter
        );
    }
    
}
