/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GraphStuff.Hong;

import DataStructures.ConditionHardBarrier;
import DataStructures.Graph;
import DataStructures.SoftBarrier;
import GraphStuff.FWBW.TrimRunnable;
import java.util.Set;

/**
 * Legacy class. No longer in use.
 * 
 * This class is intended for use as Hong's iterative trim step after the 
 * large SCC has been identified.
 * This class MUST be invoked with searchSpaceCol == trimCol. Otherwise it will 
 * waste some time decrementing the neighbor counts of some trimmed vertices.
 * @author Benjamin
 */
public class HongTrimRunnable extends TrimRunnable{
    public HongTrimRunnable(){super();}
    public HongTrimRunnable(
            Graph G, 
            Set<Integer> searchSpace,
            Set<Integer> trimSpace, 
            Set<Integer> nextIterationSet,
            int srchSpaceCol, int trimCol,
            ConditionHardBarrier hBarrier,
            SoftBarrier sBarrier,
            int threadOffset, boolean isWorker
    ){
        super(G, searchSpace, trimSpace, nextIterationSet, srchSpaceCol, trimCol,
                hBarrier, sBarrier, threadOffset, isWorker);
    }
    
    @Override
    public boolean checkColors(int color){
        return color != srchSpaceCol;
    }
}
