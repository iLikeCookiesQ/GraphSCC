/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package DataStructures.Tagging;

import DataStructures.ConditionHardBarrier;
import DataStructures.Graph;
import DataStructures.SplittableSinglyLinkedQueue;

/**
 * Tagging class that enables methods to use <T extends Colorer> as a parameter.
 * @author Benjamin
 */
public abstract class Colorer implements Runnable {
    // Intended to be used as a pseudo constructor. This avoids the need for the
    // use of Constructor reflection.
    public abstract void setup(
            SplittableSinglyLinkedQueue<Integer> coloringSpace,
            SplittableSinglyLinkedQueue<Integer>[] queues,
            int threadIdx,
            ConditionHardBarrier hBarrier,
            boolean[] visited,
            boolean isWorkerThread,
            Graph G
    );
}
