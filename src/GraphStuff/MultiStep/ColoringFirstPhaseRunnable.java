/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GraphStuff.MultiStep;

import GraphStuff.FWBW.FWBW;
import DataStructures.ConditionHardBarrier;
import DataStructures.Graph;
import DataStructures.SplittableSinglyLinkedQueue;
import DataStructures.Tagging.Colorer;
import DataStructures.Vertex;

/**
 * This Runnable allows the first phase of the coloring method to be parallelized.
 * The first phase is the part where each vertex propagates its color to all its
 * children with a lower color until no more changes happen.
 * @author Benjamin
 */

public class ColoringFirstPhaseRunnable extends Colorer {
    /**
     * Each thread grabs vertices from this queue, and does color propagation
     * for that element. If this element gave its color to a neighbor, add both
     * this element and that neighbor to the thread specific queue for next 
     * iteration. The queues array holds a queue for each thread. The main
     * combines these queues into one and then supplies it as input for the next
     * iteration.
    */
    SplittableSinglyLinkedQueue<Integer> coloringSpace; 
    // fill this with vertices eligible for the next iteration
    public  SplittableSinglyLinkedQueue<Integer>[] queues;
    public int threadIdx;
    ConditionHardBarrier hBarrier;
    public boolean[] visited;
    boolean isWorkerThread;
    
    public Graph G;
    
    // Auxiliary variables for cleaner code. Made public so extending classes can
    // access them.
    public boolean anyChildChanged;
    public int currentElem;
    public int currentCol;
    public Vertex currentVertex;
    public Vertex childVertex;
    
    public ColoringFirstPhaseRunnable(){}
    public ColoringFirstPhaseRunnable(
            SplittableSinglyLinkedQueue<Integer> coloringSpace,
            SplittableSinglyLinkedQueue<Integer>[] queues,
            int threadIdx,
            ConditionHardBarrier hBarrier,
            boolean[] visited,
            boolean isWorkerThread,
            Graph G
    ){
        setup(coloringSpace, queues, threadIdx, hBarrier, visited, isWorkerThread, G);
        /*
        this.coloringSpace = coloringSpace;
        this.queues = queues;
        this.threadIdx = threadIdx;
        this.hBarrier = hBarrier;
        this.visited = visited;
        this.isWorkerThread = isWorkerThread;
        this.G = G;//*/
    }
    
    @Override
    public final void setup(
            SplittableSinglyLinkedQueue<Integer> coloringSpace,
            SplittableSinglyLinkedQueue<Integer>[] queues,
            int threadIdx,
            ConditionHardBarrier hBarrier,
            boolean[] visited,
            boolean isWorkerThread,
            Graph G
    ){
        this.coloringSpace = coloringSpace;
        this.queues = queues;
        this.threadIdx = threadIdx;
        this.hBarrier = hBarrier;
        this.visited = visited;
        this.isWorkerThread = isWorkerThread;
        this.G = G;
    }
    
    @Override
    public void run(){
        queues[threadIdx] = new SplittableSinglyLinkedQueue();
        //while(true){
        while(!coloringSpace.isEmpty()){
            try{
                currentElem = coloringSpace.deq();
            } catch (NullPointerException e){
                if(MultiStep.DEBUG8) FWBW.debugPrint(
                        "FirstPhaseRunnable NullPointer coloringSpace.size(): " 
                        + coloringSpace.size() + " queues.length: " + queues.length
                );
                break;
            }
            
            currentVertex = G.vertices[currentElem];
            currentCol = currentVertex.color;
            
            anyChildChanged = false;
            propagateColor();
        }
        
        hBarrier.checkIn();
        for(int i: queues[threadIdx]){
            visited[i] = false;
        }
        
        hBarrier.checkIn();
        if(isWorkerThread) FWBW.checkOutThread();
    }   
    
    /**
     * This method does the actual color comparisons and assigns the appropriate
     * color to children. I've put it in a separate method instead of making a 
     * monolithic run() method because this lets me override it in another extending
     * class that implements almost the same functionality, but slightly different.
     * 
     * The WCC detection step in the Hong algorithm uses this same color propagation
     * logic, except it additionally propagates its colors to in-neighbors.
     */
    public void propagateColor(){
        for(int child: currentVertex.outNeighbours){ 
            childVertex = G.vertices[child];
            if(currentCol > childVertex.color){
                childVertex.color = currentCol;
                anyChildChanged = true;
                if(!visited[child]){
                    visited[child] = true;
                    queues[threadIdx].enq(child);
                }
            }
        }
        
        if(anyChildChanged){
            if(!visited[currentElem]){
                visited[currentElem] = true;
                queues[threadIdx].enq(currentElem);
            }
        }
    }
}
