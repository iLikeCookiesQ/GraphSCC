/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GraphStuff;

import DataStructures.ParallelizationSupport.ElementProcessor;
import java.util.Set;


/**
 * Simple class that adds an element to a set. Probably not even worth parallelizing
 * as we have to deal with the synchronization of the set. The better way to do it
 * is by adding the element to a thread local queue, and then merging the queue in
 * the main thread.
 * @author Benjamin
 */
public class SetFillerProcessor<T> implements ElementProcessor<T>{
    private final Set<T> set; // This set should be synchronized internally
    public SetFillerProcessor(Set<T> set){
        //super(0);
        this.set = set;
    }
    
    /*
    private SetFillerProcessor(Set<T> set, int threadId){
        //super(threadId);
        this.set = set;
    }//*/
    
    @Override
    public void processElement(T element) {
        set.add(element);
    }

    @Override
    public ElementProcessor<T> clone(int cloneId) {
        return new SetFillerProcessor(set
                //, cloneId
        );
    }
}
