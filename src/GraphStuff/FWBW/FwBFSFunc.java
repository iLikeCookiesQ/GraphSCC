/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GraphStuff.FWBW;

import DataStructures.Graph;
import DataStructures.SplittableSinglyLinkedQueue;
import GraphStuff.BFSFunctionInterface;
import java.util.Set;

/**
 * This class implements the BFSFunction that belongs to the forwards BFS in
 * the FW-BW algorithm.
 * @author Benjamin
 */
public class FwBFSFunc implements BFSFunctionInterface{
    Set<Integer> searchSpace;
    Set<Integer> fwdSet;
    int srchSpaceCol;
    int fwdCol;
    SplittableSinglyLinkedQueue<Integer> q;
    
    int currentElem;
    int child;
    int childCol;
    
    public FwBFSFunc(Set<Integer> searchSpace,
            Set<Integer> fwdSet,
            int srchSpaceCol,
            int fwdCol, 
            SplittableSinglyLinkedQueue<Integer> q
    ){
        this.searchSpace = searchSpace;
        this.fwdSet = fwdSet;
        this.srchSpaceCol = srchSpaceCol;
        this.fwdCol = fwdCol;
        
        this.q = q;
    }
    
    @Override
    public void process(int currentElem){
        Graph G = FWBW.G;
        if(FWBW.DEBUG) System.out.print(currentElem + ": ");
            // for all out neighbors
            for(int i = 0; i < G.vertices[currentElem].outNeighbours.length; i++){
                child = G.vertices[currentElem].outNeighbours[i];    
                childCol = G.vertices[child].color;
                if(childCol == srchSpaceCol){ // checks search space boundary
                    G.vertices[child].color = fwdCol;
                    fwdSet.add(child);
                    searchSpace.remove(child);
                    q.enq(child);     
                    if(FWBW.DEBUG){
                        System.out.print(child + " ");
                    }
                }
            }
            if(FWBW.DEBUG) System.out.print("\n");
    }
    
    @Override
    public BFSFunctionInterface createCopy(SplittableSinglyLinkedQueue<Integer> queue){
        return new FwBFSFunc(searchSpace, fwdSet, srchSpaceCol, fwdCol, queue);
    }
}
