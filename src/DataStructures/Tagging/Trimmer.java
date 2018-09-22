/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package DataStructures.Tagging;

import DataStructures.ConditionHardBarrier;
import DataStructures.Graph;
import DataStructures.ParallelizationSupport.ElementProcessor;
import DataStructures.SoftBarrier;
import DataStructures.SplittableSinglyLinkedQueue;
import java.util.Set;

/**
 * Tagging class used to make certain classes usable as method parameters in a
 * <T extends Trimmer> fashion.
 * @author Benjamin
 */
public abstract class Trimmer implements ElementProcessor<Integer>{
    // Intended to be used as a pseudo constructor. This avoids the need for the
    // use of Constructor reflection.
    public abstract void setup(
            Graph G, 
            Set<Integer> searchSpace,
            SplittableSinglyLinkedQueue<Integer>[] trimmedSets,
            int srchSpaceCol, 
            int trimCol,
            SoftBarrier sBarrier,
            boolean countOnTheFly,
            boolean seek
    );
}
