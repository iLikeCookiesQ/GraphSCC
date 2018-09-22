/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GraphStuff.MultiStep;

import GraphStuff.FWBW.FWBW;
import DataStructures.Graph;
import DataStructures.SoftBarrier;
import DataStructures.SplittableSinglyLinkedQueue;
import DataStructures.Vertex;
import java.util.Set;

/**
 * This class does a simple trim. It is intended to be used only once in a
 * program's run, as it does not update neighbor counts to support another
 * iteration. It also doesn't populate a nextIterationSet, like TrimRunnable
 * does.
 * @author Benjamin
 */
public class SimpleTrimRunnable implements Runnable{
    final SplittableSinglyLinkedQueue<Integer> queue;
    final int trimCol;
    final SoftBarrier sBarrier;
    final boolean isWorkerThread;
    
    final Set<Integer> trimSpace;
    final Graph G;
    
    public SimpleTrimRunnable(
            SplittableSinglyLinkedQueue<Integer> queue,
            int trimCol,
            SoftBarrier sBarrier,
            boolean isWorkerThread
    ){
        this.queue = queue;
        this.trimCol = trimCol;
        this.sBarrier = sBarrier;
        this.isWorkerThread = isWorkerThread;
        
        this.trimSpace = MultiStep.remainingVertices;
        this.G = FWBW.G;
    }
    
    @Override
    public void run(){
        int currentElem;
        while(!queue.isEmpty()){
            try{
                currentElem = queue.deq(); // <-- this is internally synchronized
            } catch(NullPointerException e){
                break;
            }
            Vertex currentVertex = G.vertices[currentElem];
            if(currentVertex.nIn.get() == 0 || currentVertex.nOut.get() == 0){
                if(MultiStep.DEBUG7) MultiStep.trimmedCount.getAndIncrement();
                trimSpace.remove(currentElem);
                currentVertex.color = trimCol;
                if(FWBW.TRIVIALCOUNTING) FWBW.nTrivialComponents.getAndIncrement();
            }
        }
        
        tearDown();
    }
    
    void tearDown(){
        if(isWorkerThread) {
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
}
