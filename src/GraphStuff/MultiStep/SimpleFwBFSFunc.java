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

/**
 * Does the same as GraphStuff.FWBW.FWCallback.java, but doesn't maintain a set of
 * reached elements. This isn't necessary for this one, because MultiStep does
 * not do recursive calls on the set fwdSet/sccSet. Elements are marked as
 * reached by setting their color equal to fwdCol.
 * @author Benjamin
 */
public class SimpleFwBFSFunc implements BFSFunctionInterface{
    public final SplittableSinglyLinkedQueue<Integer> q;
    public final int srchSpaceCol;
    public final int fwdCol;
     // MultiStep trimCol is INT_MAX, fwdCol is INT_MAX - 1, starting color is 0.
    final Graph G;
    
    public SimpleFwBFSFunc(
            SplittableSinglyLinkedQueue<Integer> q,
            int srchSpaceCol,
            int fwdCol
    ){
        this. q = q;
        this.srchSpaceCol = srchSpaceCol;
        this.fwdCol = fwdCol;
        
        this.G = FWBW.G;
    }
    
    @Override
    public void process(int currentElem) {
        Vertex currentVertex, childVertex;
        
        currentVertex = G.vertices[currentElem];
        for(int child: currentVertex.outNeighbours){
            childVertex = G.vertices[child];
            if(checkColors(childVertex.color)){
                //if(MultiStep.DEBUG7) FWBW.debugPrint("Adding child " + child + " with color " + childVertex.color + " to the queue.");
                childVertex.color = fwdCol;
                q.enq(child);
            }
        }
    }
    
    /**
     * Checks if the colors of this vertex make it eligible for being put on the
     * BFS queue. This is in method form so that other classes may extend this 
     * class and override this method.
     * @param color: the color to be compared against requirements.
     * @return: True if the integer id of the vertex must be put on the queue
     * based on its color and false otherwise.
     */
    public boolean checkColors(int color){
        return color == srchSpaceCol;
    }

    @Override
    public BFSFunctionInterface createCopy(SplittableSinglyLinkedQueue<Integer> q) {
        return new SimpleFwBFSFunc(q, srchSpaceCol, fwdCol);
    }
    
}
