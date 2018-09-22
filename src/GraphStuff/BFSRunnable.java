/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GraphStuff;

import GraphStuff.FWBW.FWBW;
import DataStructures.SoftBarrier;
import DataStructures.Graph;
import DataStructures.SplittableSinglyLinkedQueue;

/**
 * This class facilitates anything you want do do in a (parallelized) BFS
 * operation.
 * This class supplies a queue framework that is automatically split when it
 * gets too large. It applies the function callbackObject.process(int x) to each
 * queue element. The callback object allows you to supply any function to this
 * BFS class.
 * @author Benjamin
 */
public class BFSRunnable implements Runnable {
    // data fields in same order as constructor
    final BFSFunctionInterface callbackObject;
    final SplittableSinglyLinkedQueue<Integer> q;
    final SoftBarrier sBarrier;
    final boolean isWorkerThread;
    
    // not in constructor args
    Graph G;
    public BFSRunnable(
            BFSFunctionInterface callbackObject,
            SplittableSinglyLinkedQueue<Integer> q,
            SoftBarrier sBarrier,
            boolean isWorkerThread
    ){
        this.callbackObject = callbackObject;
        this.q = q;
        this.sBarrier = sBarrier;
        this.isWorkerThread = isWorkerThread;
        this.G = FWBW.G;
    }
    
    @Override
    public void run(){
        while(!q.isEmpty()){
            if(FWBW.PARALLELIZEBFS) manageParallelism();
            callbackObject.process(q.deq());
        }
        
        if(isWorkerThread){
            sBarrier.workerCheckOut();
            FWBW.checkOutThread();
        } else {
            try{
                sBarrier.mainCheckOut();
            } catch (Exception e){
                System.out.println(e);
                System.exit(0);
            }
        }
    }
    
    private void manageParallelism(){
        if(checkQueueSize()){
            int i = FWBW.processRequest(1);
            if(i == 1){
                createNewThread(); 
            }
        }
    }
    
    private boolean checkQueueSize(){
        return (q.size() > FWBW.BFSQueueSplittingThreshold);
    }
    
    private void createNewThread(){
        SplittableSinglyLinkedQueue<Integer> q2 = q.split();
        // create a copy of the callback object and insert the new queue
        BFSFunctionInterface callbackObj2 = callbackObject.createCopy(q2);
        BFSRunnable r = new BFSRunnable(callbackObj2, q2, sBarrier, true);
        
        Thread thread = new Thread(r);
        sBarrier.workerCheckIn();
        if(FWBW.DEBUG6) FWBW.debugPrint("Split BFS queue. Starting worker BFSRunnable.");
        thread.start();
    }
}
