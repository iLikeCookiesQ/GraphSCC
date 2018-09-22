/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package DataStructures;
/**
 * Implements a "soft" barrier, where only the main thread has to wait for worker
 * threads to finish.
 * @author Benjamin
 */
public class SoftBarrier {
    private int nWorkersRunning;
    private final Object syncObj;
    
    
    public SoftBarrier(){
        nWorkersRunning = 0;
        syncObj = new Object();
    }
    
    
    public void workerCheckIn(){
        synchronized(syncObj){
            ++nWorkersRunning;
        }
    }
    public void workerCheckOut(){
        synchronized(syncObj){
            if(--nWorkersRunning == 0){
                syncObj.notify();
            }
        }
    }
    public void mainCheckOut() throws InterruptedException {
        synchronized(syncObj){
            if(nWorkersRunning == 0) return;
            syncObj.wait();
        }
    }
}
