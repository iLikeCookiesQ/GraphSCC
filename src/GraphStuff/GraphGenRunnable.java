/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GraphStuff;

import GraphStuff.Probability.ProbabilityBonusFunction;
import DataStructures.ConditionHardBarrier;
import GraphStuff.RandomGraphGen.WrapSet;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author Benjamin
 */
public class GraphGenRunnable implements Runnable{
    WrapSet[] outNeighbors, inNeighbors;
    double p;
    ProbabilityBonusFunction[] bonuses;
    ConditionHardBarrier barrier;
    int threadId;
    AtomicInteger currentIdx;
    
    final boolean DEBUG = true; // prints message of completed rows per 1000 rows
    final int N;
    
    GraphGenRunnable(
            WrapSet[] outNeighbors,
            WrapSet[] inNeighbors,
            double p,
            ProbabilityBonusFunction[] bonuses,
            ConditionHardBarrier barrier,
            int threadId,
            AtomicInteger currentIdx
    ){
        this.inNeighbors = inNeighbors;
        this.outNeighbors = outNeighbors;
        this.p = p;
        this.bonuses = bonuses;
        this.barrier = barrier;
        this.threadId = threadId;
        this.currentIdx = currentIdx;
        
        this.N = outNeighbors.length;
    }
    
    @Override
    public void run(){
        //Thread.currentThread().setPriority(10);
        grabWhenever(10);
        barrier.checkIn();
    }
    
    // split the array in consecutive sections
    void splitArray(){
        int size = outNeighbors.length;
        int nThreads = barrier.getThreadCount();
        int leftBoundary = (int)((size/(double)nThreads)*threadId);
        int rightBoundary = (int)((size/(double)nThreads)*(threadId + 1));
        
        // auxiliary variables
        Random rnd = new Random(System.currentTimeMillis());
        barrier.checkIn();
        int nDone = 0;
        
        double chance;
        // for each vertex
        for(int i = leftBoundary; i < rightBoundary; i++){
            // for each other vertex
            for(int j = 0; j < size; j++){
                chance = p + getBonuses(i, j);
                if(i == j) continue;
                if(chance > rnd.nextDouble()){
                    outNeighbors[i].set.add(j);
                    inNeighbors[j].set.add(i);
                }
            }
            if(DEBUG){
                if(++nDone%1000 == 0) System.out.println(Thread.currentThread().getName() + " work done " + nDone);
            }
        }
    }
    
    // each thread takes elements who have i%nThreads = threadId
    void gratedArray(){
        int size = outNeighbors.length;
        int nThreads = barrier.getThreadCount();
        
        // auxiliary variables
        Random rnd = new Random(System.currentTimeMillis());
        int nDone = 0;
        
        double chance;
        // for each vertex
        for(int i = threadId; i < size; i+= nThreads){
            // for each other vertex
            for(int j = 0; j < size; j++){
                chance = p + getBonuses(i, j);
                if(i == j) continue;
                if(chance > rnd.nextDouble()){
                    outNeighbors[i].set.add(j);
                    inNeighbors[j].set.add(i);
                }
            }
            if(DEBUG){
                if(++nDone%1000 == 0) System.out.println(Thread.currentThread().getName() + " work done " + nDone);
            }
        }
    }
    
    // Use a getAndIncrement on currentIdx. This method involves less waiting on slow threads.
    void grabWhenever(){
        int size = outNeighbors.length;   
        
        // auxiliary variables
        Random rnd = new Random(System.currentTimeMillis());       
        int nDone = 0;
        int i;
        
        double chance;
        while(true){
            i = currentIdx.getAndIncrement();
            if(i >= size) break;
            // for each other vertex
            for(int j = 0; j < size; j++){
                chance = p + getBonuses(i, j);
                if(i == j) continue;
                if(chance > rnd.nextDouble()){
                    outNeighbors[i].set.add(j);
                    inNeighbors[j].set.add(i);
                }
            }
            if(DEBUG){
                if(++nDone%1000 == 0) System.out.println(Thread.currentThread().getName() + " work done " + nDone);
            }
        }
    }
    
    // The nPerGrab is the amount of elements are assigned per getAndIncrement of
    // currentIdx. This is supposed to reduce the amount of contention on the 
    // hardware lock. This could matter if many threads are used.
    void grabWhenever(int nPerGrab){
        int size = outNeighbors.length;   
        
        // auxiliary variables
        Random rnd = new Random(System.currentTimeMillis());
        //barrier.checkIn();
        int nDone = 0;
        int i;
        
        double chance;
        while(true){
            i = currentIdx.getAndIncrement();
            if(i >= Math.ceil(size/(double)nPerGrab)) break; // 
            int start = i*nPerGrab;
            int end = start + nPerGrab;
            try{
                
                // for each of the nPerGrab vertices
                for(int k = start; k < end; k++){
                    // for each other vertex
                    for(int j = 0; j < size; j++){
                        chance = p + getBonuses(k, j);
                        if(k == j) continue;
                        if(chance > rnd.nextDouble()){
                            outNeighbors[k].set.add(j);
                            inNeighbors[j].set.add(k);
                        }
                    }
                    if(DEBUG){
                        if(++nDone%1000 == 0) System.out.println(
                                Thread.currentThread().getName() + " work done " + nDone);
                    }
                }
            } catch(ArrayIndexOutOfBoundsException e){
                System.out.println(e);
            }   
        }
    }
    
    double getBonuses(int startVertex, int endVertex){
        double returnValue = 0;
        for(ProbabilityBonusFunction pf: bonuses){
            switch(pf.getBonusType()){
                case 0: // doubly dependent bonus
                    returnValue += pf.get2DepBonus(startVertex, endVertex);
                    break;
                case 1: // depends only on startVertex
                    returnValue += pf.getSDepBonus(startVertex);
                    break;
                case 2: // depends only on endVertex
                    returnValue += pf.getEDepBonus(endVertex);
                    break;
                case 3: // does not depend on any vertex id
                    returnValue += pf.getInDepBonus();
                    break;
                default: 
                    System.out.println("Illegal State: Wrong bonusType int");
                    System.exit(0);
            }
        }
        return returnValue;
    }
}
