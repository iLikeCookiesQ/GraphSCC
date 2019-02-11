/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package DataStructures.ParallelizationSupport;

/**
 * Interface that is used by IterationRunnable to process individual elements in
 * the Iterator being iterated.
 * @author Benjamin
 * @param <T>: The type of elements being iterated over.
 */
public interface ElementProcessor<T> {
    /**
     * threadId can be used to assign a specific data storage location to a 
     * specific thread. For example, the threadId could be the index in an array 
     * that threads write to. The index would then denote that threads local space
     */
    
    public  void processElement(T element);
    public abstract ElementProcessor<T> clone(int cloneId);
}
