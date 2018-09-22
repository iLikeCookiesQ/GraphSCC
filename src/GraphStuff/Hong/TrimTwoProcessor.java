/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GraphStuff.Hong;

import DataStructures.ConditionHardBarrier;
import DataStructures.Graph;
import DataStructures.ParallelizationSupport.ElementProcessor;
import DataStructures.SoftBarrier;
import DataStructures.SplittableSinglyLinkedQueue;
import GraphStuff.FWBW.FWBW;
import GraphStuff.FWBW.TrimRunnable;
import GraphStuff.IterativeTrimProcessor;
import GraphStuff.MultiStep.MultiStep;
import java.util.Set;


/**
 *
 * @author Benjamin
 */
public class TrimTwoProcessor extends IterativeTrimProcessor{
    public TrimTwoProcessor(){super();}
    TrimTwoProcessor(
            Graph G, 
            Set<Integer> searchSpace,
            SplittableSinglyLinkedQueue<Integer>[] trimmedSets,
            int srchSpaceCol,
            int trimCol,
            SoftBarrier sBarrier,
            boolean countOnTheFly,
            boolean seek
    ){
        super(
                G, searchSpace, trimmedSets,
                srchSpaceCol, trimCol,
                sBarrier, countOnTheFly,
                seek
        );
    }
    
    private TrimTwoProcessor(
            Graph G,
            Set<Integer> searchSpace,
            SplittableSinglyLinkedQueue<Integer> trimmedSet,
            int srchSpaceCol,
            int trimCol,
            SoftBarrier sBarrier,
            boolean countOnTheFly,
            boolean seek     
    ){
        /*
        super(G, searchSpace, trimmedSet, srchSpaceCol,
        trimCol, sBarrier, countOnTheFly, seek);//*/
        
        this.G = G;
        this.searchSpace = searchSpace;
        this.trimmedSet = trimmedSet;
        this.srchSpaceCol = srchSpaceCol;
        this.trimCol = trimCol;
        this.sBarrier = sBarrier;
        this.countOnTheFly = countOnTheFly;
        this.seek = seek;//*/
    }
    
    int otherVertex;
    boolean connectsBack;
    //int neighborCounter;
    @Override
    public void processElement(Integer element){
        auxVertex = G.vertices[element];
        // Recount out neighbors during this trim.
        if(countOnTheFly) auxVertex.nOut.set(countNeighbors(auxVertex.outNeighbours));
        
        otherVertex = -1;
        // Check if this vertex is in a size 2 scc with an out neighbor.
        if(auxVertex.nOut.get() == 1){
            for(int child: auxVertex.outNeighbours){
                if(checkColors(G.vertices[child].color)){
                    otherVertex = child;
                    break;
                }
            }
            if(MultiStep.DEBUG7) if(otherVertex == -1) FWBW.debugPrint("otherVertex == -1");
            connectsBack = false;
            for(int parent: auxVertex.inNeighbours){
                if(parent == otherVertex) connectsBack = true;
                break;
            }
            if(connectsBack){
                // Count the out neighbors of the lone neighbor.
                if(countOnTheFly) G.vertices[otherVertex].nOut.set(countNeighbors(G.vertices[otherVertex].outNeighbours));
                
                if(G.vertices[otherVertex].nOut.get() == 1){
                    trimTheseTwo(element, otherVertex);
                    return;
                }
            }
        }
        
        // Recount in neighbors.
        if(countOnTheFly) auxVertex.nIn.set(countNeighbors(auxVertex.inNeighbours));
        
        otherVertex = -1;
        // Check if this vertex is in a size 2 scc with an in neighbor.
        if(auxVertex.nIn.get() == 1){
            for(int parent: auxVertex.inNeighbours){
                if(checkColors(G.vertices[parent].color)){
                    otherVertex = parent;
                    break;
                }
            }
            if(MultiStep.DEBUG7) if(otherVertex == -1) FWBW.debugPrint("otherVertex == -1");
            connectsBack = false;
            for(int child: auxVertex.outNeighbours){
                if(child == otherVertex) connectsBack = true;
                break;
            }
            if(connectsBack){
                if(countOnTheFly) G.vertices[otherVertex].nIn.set(countNeighbors(G.vertices[otherVertex].inNeighbours));
                
                if(G.vertices[otherVertex].nIn.get() == 1){
                    trimTheseTwo(element, otherVertex);
                }
            }
        }
    }
    
    
    int lowest;
    private void trimTheseTwo(int a, int b){
        if(a < b) lowest = a;
        else lowest = b;
        
        synchronized(G.vertices[lowest]){
            if(G.vertices[lowest].color == trimCol) return;
            G.vertices[a].color = trimCol;
            G.vertices[b].color = trimCol;
        }
        
        searchSpace.remove(a);
        searchSpace.remove(b);
        trimmedSet.enq(a);
        trimmedSet.enq(b);
        if(FWBW.DEBUG2) {
            FWBW.debugPrint("TrimTwo removed pair " + a + " " + b);
        }
        try {
            Set<Integer> scc = FWBW.setType.getClass().newInstance();
            scc.add(a);
            scc.add(b);
            if(Hong.DEBUG7){
                FWBW.debugPrint("TrimTwo found an SCC: " + a + " " + b);
            }
            FWBW.outputSCC(scc);
        } catch (Exception e) {System.out.println(e);System.exit(0);}  
    }
    
    @Override
    public ElementProcessor<Integer> clone(int cloneId) {
        return new TrimTwoProcessor(
            G, 
            searchSpace, 
            trimmedSets[cloneId],
            srchSpaceCol, 
            trimCol, 
            sBarrier,
            countOnTheFly,
            seek
        );
    }
}
