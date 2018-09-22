/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GraphStuff.Hong;

import DataStructures.SoftBarrier;
import DataStructures.SplittableSinglyLinkedQueue;
import GraphStuff.FWBW.FWBW;
import GraphStuff.FWBW.FWBWRunnable;
import java.util.HashMap;
import java.util.Set;

/**
 * This Runnable empties the WCC queue, which is a queue of unique WCC colors.
 * Each color represents a WCC.
 * @author Benjamin
 */
public class RecFWBWRunnable implements Runnable{
    final SplittableSinglyLinkedQueue<Integer> WCCQueue;
    final HashMap<Integer, Set<Integer>> map;
    final SoftBarrier sBarrier;
    final boolean isWorkerThread;
    
    public RecFWBWRunnable(
            SplittableSinglyLinkedQueue<Integer> WCCQueue,
            HashMap<Integer, Set<Integer>> map,
            SoftBarrier sBarrier,
            boolean isWorkerThread
    ){
        this.WCCQueue = WCCQueue;
        this.map = map;
        this.sBarrier = sBarrier;
        this.isWorkerThread = isWorkerThread;
    }
    
    int currentCol;
    @Override
    public void run() {
        while(!WCCQueue.isEmpty()){
            try{
                currentCol = WCCQueue.deq();
            } catch(NullPointerException e){
                break;
            }
            new FWBWRunnable(
                    map.get(currentCol),
                    currentCol,
                    false
            ).run();
        }
        
        tearDown();
    }
    
    
    private void tearDown(){
        if(isWorkerThread) {
            //System.out.println("===============Checking out worker RecFWBWRunnable");
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
