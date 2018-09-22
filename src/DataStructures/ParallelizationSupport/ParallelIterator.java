/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package DataStructures.ParallelizationSupport;

import DataStructures.SoftBarrier;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This class implements parallel iteration over an Iterable.
 * @author Benjamin
 * @param <T>: The type of element in the Iterable that is to be iterated over.
 */
public class ParallelIterator<T> {
    private final Iterable<T> iterable;
    private final int size;
    private final int nThreads; // The number of parallel threads including the main thread.
    /**
     * Defines what we do with each element. Each runnable gets a separate instance
     * of this callback object that has a unique threadId. This makes it so the 
     * Runnables don't read/write to the same variables in the callback object.
     */
    private final ElementProcessor<T> callbackObj; 
    
    public ParallelIterator(Iterable iterable, int size, int nThreads, ElementProcessor<T> ep){
        this.iterable = iterable; // The collection we want to iterate over.
        this.size = size; // Size of that collection.
        this.nThreads = nThreads;
        this.callbackObj = ep;
    }
    
    public void iterate(){
        IterationRunnable[] runnables = new IterationRunnable[nThreads - 1];
        Thread[] threads = new Thread[nThreads - 1];
        SoftBarrier sBarrier = new SoftBarrier();
        AtomicInteger globalCurrentIndex = new AtomicInteger(0);
        for(int i = 0; i < nThreads - 1; i++){
            runnables[i] = new IterationRunnable(globalCurrentIndex, iterable.iterator(), size, callbackObj.clone(i + 1), sBarrier, true);
            threads[i] = new Thread(runnables[i]);
        }
        
        IterationRunnable ir = new IterationRunnable(globalCurrentIndex, iterable.iterator(), size, callbackObj, sBarrier, false);
        
        for(Thread thread: threads) {
            sBarrier.workerCheckIn();
            thread.start();
        }
        ir.run();
    }
}
