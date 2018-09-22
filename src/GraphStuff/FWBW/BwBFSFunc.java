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
 * This class implements the BFSFunction that belongs to the backwards BFS in
 * the FW-BW algorithm.
 * @author Benjamin
 */
public class BwBFSFunc implements BFSFunctionInterface{
    Set<Integer> searchSpace;
    Set<Integer> fwdSet;
    Set<Integer> bwdSet;
    Set<Integer> sccSet;
    int srchSpaceCol;
    int fwdCol;
    int bwdCol;
    int sccCol;
    
    SplittableSinglyLinkedQueue<Integer> q;
    int currentElem;
    
    int parent;
    int parentCol;
    
    public BwBFSFunc(Set<Integer> searchSpace,
            Set<Integer> fwdSet,
            Set<Integer> bwdSet,
            Set<Integer> sccSet,
            int srchSpaceCol,
            int fwdCol, 
            int bwdCol,
            int sccCol,
            SplittableSinglyLinkedQueue<Integer> q
    ){
        this.searchSpace = searchSpace;
        this.fwdSet = fwdSet;
        this.bwdSet = bwdSet;
        this.sccSet = sccSet;
        this.srchSpaceCol = srchSpaceCol;
        this.fwdCol = fwdCol;
        this.bwdCol = bwdCol;
        this.sccCol = sccCol;
        
        this.q = q;
    }
    
    @Override
    public void process(int currentElem){
        Graph G = FWBW.G;
        // for all in neighbors
            for(int i = 0; i < G.vertices[currentElem].inNeighbours.length; i++){
                parent = G.vertices[currentElem].inNeighbours[i];
                parentCol = G.vertices[parent].color;
                if(parentCol == fwdCol){ // was in forward search
                    G.vertices[parent].color = sccCol;
                    sccSet.add(parent);
                    fwdSet.remove(parent);
                    q.enq(parent);
                } else if (parentCol == srchSpaceCol){ // was not in forward search
                    G.vertices[parent].color = bwdCol;
                    bwdSet.add(parent);
                    searchSpace.remove(parent);
                    q.enq(parent);                            
                } 
            }
    }
    
    @Override
    public BFSFunctionInterface createCopy(SplittableSinglyLinkedQueue<Integer> queue){
        return new BwBFSFunc(searchSpace, fwdSet, bwdSet, sccSet, 
                srchSpaceCol, fwdCol, bwdCol, sccCol, queue);
    }
}
