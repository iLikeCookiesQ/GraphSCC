/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GraphStuff;

import DataStructures.SplittableSinglyLinkedQueue;
/**
 * This interface is used by the BFSRunnable class to determine what to do with
 * each dequeued element of the BFS queue.
 * @author Benjamin
 */
public interface BFSFunctionInterface {
    public void process(int currentElem);
    public BFSFunctionInterface createCopy(SplittableSinglyLinkedQueue<Integer> q);
}
