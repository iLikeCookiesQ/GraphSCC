/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GraphStuff.Hong;

import DataStructures.ConditionHardBarrier;
import DataStructures.Graph;
import DataStructures.SplittableSinglyLinkedQueue;
import GraphStuff.MultiStep.ColoringFirstPhaseRunnable;



/**
 * This class implements parallelizable functionality for detecting Weakly Connected
 * Components (WCCs).
 * The code is very similar to ColoringFirstPhaseRunnable, except it propagates
 * colors to both in- and out-neighbors. Instead of only out-neighbors. It overrides
 * the part where the propagation happens to implement this difference.
 * 
 * @post
 * The remaining vertices all have the color of the root of their respective WCC.
 * 
 * To get the WCCs in a set form, you must manually sweep over them to look
 * at their colors. Recommended is to create a HashMap<Integer, HashSet<Integer>>.
 * It would map the identifier for each WCC (the color of vertices in that WCC,
 * as well as the original color of the root of that WCC) to a set of vertices
 * that belongs to that WCC.
 * @author Benjamin
 */
public class WCCRunnable extends ColoringFirstPhaseRunnable{
    public WCCRunnable(){super();}
    WCCRunnable(
            SplittableSinglyLinkedQueue<Integer> coloringSpace,
            SplittableSinglyLinkedQueue<Integer>[] queues,
            int threadIdx,
            ConditionHardBarrier hBarrier,
            boolean[] visited,
            boolean isWorkerThread,
            Graph G
    ){
        super(
                coloringSpace,
                queues,
                threadIdx,
                hBarrier,
                visited,
                isWorkerThread,
                G
        );
    }
    
    @Override
    public void propagateColor(){
        // copied code from overridded method
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
        
        // the important bit
        for(int parent: currentVertex.inNeighbours){ 
            childVertex = G.vertices[parent];
            if(currentCol > childVertex.color){
                childVertex.color = currentCol;
                anyChildChanged = true;
                if(!visited[parent]){
                    visited[parent] = true;
                    queues[threadIdx].enq(parent);
                }
            }
        }
        
        // copied code from overridden method
        if(anyChildChanged){
            if(!visited[currentElem]){
                visited[currentElem] = true;
                queues[threadIdx].enq(currentElem);
            }
        }
    }
}
