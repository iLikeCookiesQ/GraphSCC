/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GraphStuff.MultiStep;

import DataStructures.Graph;
import DataStructures.SplittableSinglyLinkedQueue;
import DataStructures.Vertex;
import GraphStuff.BFSFunctionInterface;
import GraphStuff.FWBW.FWBW;
import java.util.Set;

/**
 * Does the same as GraphStuff.FWBW.BWCallback.java, but doesn't maintain a set
 * of reached elements. This isn't necessary because there are no recursive calls
 * onto a set bwdSet in MultiStep. This marks every reached vertex that has color 
 * srchSpaceCol with color sccCol, and adds those vertices to the set sccSet.
 * 
 * This also doesn't add elements whose color isn't srchSpaceCol to the BFS queue,
 * unlike FWBW.FWCallback.java, as a backwards set that isn't part of the SCC
 * isn't needed for any recursive calls.
 * @author Benjamin
 */
public class SimpleBwBFSFunc implements BFSFunctionInterface{
    final SplittableSinglyLinkedQueue<Integer> queue;
    final Set<Integer> sccSet;
    final int fwdCol;
    final int sccCol;
    final Set<Integer> remainingVertices;
    
    final Graph G;
    
    public SimpleBwBFSFunc(
            SplittableSinglyLinkedQueue<Integer> queue,
            Set<Integer> sccSet,
            int fwdCol,
            int sccCol,
            Set<Integer> remainingVertices
    ){
        this.queue = queue;
        this.sccSet = sccSet;
        this.fwdCol = fwdCol;
        this.sccCol = sccCol;
        this.remainingVertices = remainingVertices;
        
        this.G = FWBW.G;
    }

    @Override
    public void process(int currentElem) {
        int parentCol;
        Vertex currentVertex;
        Vertex parentVertex;
        
        currentVertex = G.vertices[currentElem];
        for(int parent: currentVertex.inNeighbours){
            parentVertex = G.vertices[parent];
            parentCol = G.vertices[parent].color;
            if(parentCol == fwdCol){
                parentVertex.color = sccCol;
                sccSet.add(parent);
                queue.enq(parent);
                remainingVertices.remove(parent);
            }
        }
    }

    @Override
    public BFSFunctionInterface createCopy(SplittableSinglyLinkedQueue<Integer> q) {
        return new SimpleBwBFSFunc(q, sccSet, fwdCol, sccCol, remainingVertices);
    }
}
