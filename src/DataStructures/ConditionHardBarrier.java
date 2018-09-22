/*
 * 
 */
package DataStructures;
import GraphStuff.FWBW.FWBW;
import GraphStuff.MultiStep.MultiStep;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
/**
 * Implements a "hard" barrier that makes each thread wait for all other threads
 * before continuing. 
 * No spinning involved. Instead use a signalAll() to wake up other threads.
 * @author Benjamin
 */
public class ConditionHardBarrier {
    private final AtomicInteger nUnderway;
    private final ReentrantLock lock;
    private final Condition lastOneArrived;
    private final int threadCount;
    public ConditionHardBarrier(int threadCount){
        this.threadCount = threadCount;
        nUnderway = new AtomicInteger(threadCount);
        lock = new ReentrantLock();
        lastOneArrived = lock.newCondition();
    }
    
    private void reset(){
        nUnderway.set(threadCount);
    }
    
    // call when the thread arrives at the barrier
    public void checkIn(){
        lock.lock();
        try{            
            if(nUnderway.decrementAndGet() == 0) {                
                reset();
                if(MultiStep.DEBUG8) FWBW.debugPrint("All arrived at barrier!");
                lastOneArrived.signalAll();
            } else {                
                lastOneArrived.await();
            }
        } catch(Exception e) {
            System.out.println("ERROR: THREAD INTERRUPTED: " + e);
        } finally {
            lock.unlock();
        }
    }
    
    public int getThreadCount(){
        return threadCount;
    }
    
}
