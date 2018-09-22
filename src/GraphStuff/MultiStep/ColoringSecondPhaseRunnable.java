/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GraphStuff.MultiStep;

import GraphStuff.FWBW.FWBW;
import DataStructures.SoftBarrier;
import DataStructures.SplittableSinglyLinkedQueue;
import DataStructures.TwoInts;
import GraphStuff.BFSFunctionInterface;
import GraphStuff.BFSRunnable;
import java.util.Set;

/**
 * This Runnable implements the second phase of coloring, where a backwards BFS
 * is done from each of the "roots".
 * @author Benjamin
 */
public class ColoringSecondPhaseRunnable implements Runnable{
    final SplittableSinglyLinkedQueue<TwoInts> specialRoots;
    final SplittableSinglyLinkedQueue<Integer> roots;
    final int sccCol;
    final SoftBarrier sBarrier;
    final boolean isWorkerThread;
    
    // the variables below keep track of what vertices have been visited by
    // this BFS. The list field helps more efficient resetting of the flags.
    //final AtomicBoolean[] visited;
    //final SplittableSinglyLinkedQueue<Integer> visitedList;
    
    ColoringSecondPhaseRunnable(
            SplittableSinglyLinkedQueue<TwoInts> specialRoots,
            SplittableSinglyLinkedQueue<Integer> roots,
            int sccCol,
            SoftBarrier sBarrier,
            boolean isWorkerThread
    ){
        this.specialRoots = specialRoots;
        this.roots = roots;
        this.sccCol = sccCol;
        this.sBarrier = sBarrier;
        this.isWorkerThread = isWorkerThread;
    }
    
    @Override
    public void run(){
        int currentElem;
        TwoInts ti;
        
        // First do special roots.
        while(!specialRoots.isEmpty()){
            try{
                ti = specialRoots.deq();
                currentElem = ti.vertexId;
            } catch (NullPointerException e){
                break;
            }
            
            if(MultiStep.DEBUG8) FWBW.debugPrint("Doing backwards search from special root.");
            processElement(currentElem, ti.statistic); // color of special root stored in the TwoInts data carrier.
        }
        if(MultiStep.DEBUG7){
            if(!isWorkerThread){
                FWBW.debugPrint("ColoringSecondPhaseRunnable main thread reached end of special roots queue.");
            }
        }
        
        // Then do rest.
        while(!roots.isEmpty()){
            try{
                currentElem = roots.deq();
            } catch (NullPointerException e){
                if(MultiStep.DEBUG8) FWBW.debugPrint("ColoringSecondPhaseRunnable end of normal roots queue.");
                break;
            }
            
            processElement(currentElem, currentElem); // color of root currentElem is itself.
        }
        
        tearDown();
    }
    
    void processElement(int currentElem, int srchSpaceCol){
        SplittableSinglyLinkedQueue<Integer> q = new SplittableSinglyLinkedQueue();
        Set<Integer> sccSet = null;
        try {
            sccSet = FWBW.setType.getClass().newInstance();
        } catch (Exception e) {System.out.println(e); System.exit(0);}
        q.enq(currentElem);
        sccSet.add(currentElem);
        FWBW.G.vertices[currentElem].color = sccCol;
        MultiStep.remainingVertices.remove(currentElem);
        
        SoftBarrier sBarrier2 = new SoftBarrier();
         
        BFSFunctionInterface callbackObject = new  SimpleBwBFSFunc(
                q, 
                sccSet, 
                srchSpaceCol,
                sccCol,
                MultiStep.remainingVertices
        );
        BFSRunnable r = new BFSRunnable(callbackObject, q, sBarrier2, false);
        r.run();
        
        if(MultiStep.DEBUG7) if(sccSet.size() > 1) FWBW.debugPrint("Coloring found SCC of size " + sccSet.size());
        FWBW.outputSCC(sccSet);
    }
    
    private void tearDown(){
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
