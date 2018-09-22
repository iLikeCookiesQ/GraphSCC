/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package DataStructures.ParallelizationSupport;

import DataStructures.SoftBarrier;
import GraphStuff.FWBW.FWBW;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Runnable class that implements nPerGrab logic while traversing an Iterator
 * @author Benjamin
 */
public class IterationRunnable<T> implements Runnable{
    // nPerGrab is the amount of elements we process per getAndIncrement.
    // This reduces contention on the hardware lock, while at the same time
    // reducing the maximum wait time for slow threads to a minimum.
    final int nPerGrab = 5;
    
    final AtomicInteger globalCurrentPartitionIndex;
    final Iterator<T> iterator;
    //final int size;
    final ElementProcessor<T> ep;
    final SoftBarrier sBarrier;
    final boolean isWorker;
    
    /**
     * The first partition whose first element would be out of bounds.
     * The partition before it is the last partition.
     */
    final int boundaryGlobalPartition;
    
    IterationRunnable(
            AtomicInteger globalCurrentIndex,
            Iterator<T> iterator, 
            int size,
            ElementProcessor<T> ep, 
            SoftBarrier sBarrier,
            boolean isWorker
    ){
        this.globalCurrentPartitionIndex = globalCurrentIndex;
        this.iterator = iterator;
        //this.size = size;
        this.ep = ep;
        this.sBarrier = sBarrier;
        this.isWorker = isWorker;
        
        this.boundaryGlobalPartition = (int)Math.ceil(size/(double)nPerGrab);
    }
    
    int lastKnownAssignedPartition = 0;
    int assignedPartition = 0;
    @Override
    public void run() {
        while(true){
            assignedPartition = globalCurrentPartitionIndex.getAndIncrement();
            /*// Old code
            if(assignedPartition >= boundaryGlobalPartition - 1) {
                if(isWorker) { // Worker threads stop here.
                    break;
                } else { // The main thread does the final partition.
                    assignedPartition = boundaryGlobalPartition - 1;
                    catchUpIteratorState();
                    processFinalPartition();
                    break;
                }
            } //*/
            
            if(assignedPartition == boundaryGlobalPartition - 1){
                // Deal with the last partition.
                catchUpIteratorState();
                processFinalPartition();
                break;
            } else if (assignedPartition > boundaryGlobalPartition - 1){
                /**
                 * The last partition is taken by another thread.
                 * All partitions are assigned to a thread or finished.
                 */
                break;
            }
            
            catchUpIteratorState();
            processPartition();
        }
        tearDown();
    }
    
    /**
     * Catches up the iterator state to where they need to
     * be with regard to globalCurrentIndex.
     */
    int difference;
    void catchUpIteratorState(){
        difference = assignedPartition - lastKnownAssignedPartition;
        
        // Skip a number of partitions equal to difference. Other threads have processed these.
        for(int i = 0; i < difference*nPerGrab; i++){
            iterator.next();
        }
    }
    
    /**
     * Processes the nPerGrab consecutive elements in the assigned partition.
     */
    void processPartition(){
        for(int i = 0; i < nPerGrab; i++){
            ep.processElement(iterator.next());
        }
        // As we have moved up the iterator by one partition during this step, 
        // we update our last partition pointer by putting it one further than
        // the id of the just processed partition.
        lastKnownAssignedPartition = assignedPartition + 1;
    }
    
    /**
     * Processes the the last few elements in the last partition.
     */
    void processFinalPartition(){
        while(iterator.hasNext()){
            ep.processElement(iterator.next());
        }
    }
    
    // handle barriers.
    void tearDown(){
        if(isWorker) {
            sBarrier.workerCheckOut();
            //FWBW.checkOutThread();  // < == if this part gives an error simply comment it.
                                    // If you happen to be someone using my code for general 
                                    // purposes, you don't need this checkOutThread() statement.
        } else {
            try{
                sBarrier.mainCheckOut();
            } catch (Exception e){
                System.out.println(e);
                System.exit(0);
            }
        }
    }
}
