/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GraphStuff.Hong;

import DataStructures.Graph;
import DataStructures.SplittableSinglyLinkedQueue;
import DataStructures.Vertex;
import GraphStuff.BFSFunctionInterface;
import GraphStuff.FWBW.FWBW;
import GraphStuff.MultiStep.SimpleFwBFSFunc;

/**
 * Does the same as GraphStuff.FWBW.FWCallback.java, but doesn't maintain a set of
 * reached elements. This isn't necessary for this one, because MultiStep does
 not do recursive calls on the set fwdSet/sccSet. Elements are marked as
 reached by setting their color equal to fwdCol.
 
 This is different from MultiStep.SimpleFwBFSFunc in the sense that this avoids
 vertices with color srchSpaceCol, instead of explicitly only adding vertices with
 that color to the queue. This allows the parallel FWBW phase in Hong to
 start a new FWBW iteration without the fwdSet from the old iteration being excluded
 from the forwards BFS. (That set has been colored in the old iteration and remains
 colored in the current. Those vertices must still be put on the queue of the 
 BFS of the current iteration.)
 * 
 * @author Benjamin
 */
public class HongSimpleFwBFSFunc extends SimpleFwBFSFunc{
    /*
    final SplittableSinglyLinkedQueue<Integer> q;
    final int avoidCol;
    final int fwdCol;
     // MultiStep trimCol is INT_MAX, fwdCol is INT_MAX - 1, starting color is 0.
    final Graph G;
    //*/
    HongSimpleFwBFSFunc(
            SplittableSinglyLinkedQueue<Integer> q,
            int srchSpaceCol,
            int fwdCol
    ){
        super(q, srchSpaceCol, fwdCol);
    }
    
    @Override
    public boolean checkColors(int color){
        return color != srchSpaceCol && color != fwdCol;
    }

    @Override
    public BFSFunctionInterface createCopy(SplittableSinglyLinkedQueue<Integer> q) {
        return new HongSimpleFwBFSFunc(q, srchSpaceCol, fwdCol);
    }
}
