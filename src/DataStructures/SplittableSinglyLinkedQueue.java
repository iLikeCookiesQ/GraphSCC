/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package DataStructures;

import java.util.Iterator;

/**
 * This class implements a queue that can be efficiently split without having
 * to copy or clear existing data.
 * Not robust: The user must pay attention to not access null data fields.
 * Is concurrency safe as locks are acquired inside this class's methods.
 * (Safe for enq() and deq() methods only)
 * @author Benjamin
 */


public class SplittableSinglyLinkedQueue<T> implements Iterable<T>{
    private final boolean DEBUG = false;
    
    // State
    private Entry<T> head;
    private Entry<T> tail;
    private int size;
    private final Object deqLock = new Object();
    private final Object enqLock = new Object();
    
    // Constructors
    private SplittableSinglyLinkedQueue(Entry<T> head, Entry<T> tail, int size){
        this.head = new Entry();
        this.head.next = head;
        this.tail = tail;
        this.size = size;
    }
    public SplittableSinglyLinkedQueue(){
        head = new Entry();
        tail = head;
        size = 0;
    }
    
    // Methods
    public void enq(T t){
        synchronized(enqLock){
            tail.next = new Entry(t);
            tail = tail.next;
            size++;
        }
    }
    
    public T deq() {
        synchronized(deqLock){
            T data = head.next.data;
            if(data == null){
                throw new NullPointerException();
            }
            head = head.next;
            size--;
            return data;
        }
    }
    
    // Cuts off the 2nd half of the queue, and returns it as a new queue.
    // The original queue no longer has the 2nd half attached.
    public SplittableSinglyLinkedQueue<T> split(){ //--------------------------------------HAS BUG: Somehow the tail pointer of some queue becomes null---------
        int currentIdx = 0;
        int target = size/2;
        Entry splitHere = head;
        if(DEBUG) System.out.println("target: " + target);
        while(currentIdx < target){
            splitHere = splitHere.next;
            currentIdx ++;
        }
        int latterSize = (int)Math.ceil(size/2.0);
        SplittableSinglyLinkedQueue latterHalf = new SplittableSinglyLinkedQueue(splitHere.next, tail, latterSize);
        size = size - latterSize;
        //splitHere.next = new Entry();
        //tail = splitHere.next;
        tail = splitHere;
        tail.next = null;
        
        return latterHalf;
    }
    
    // splits the queue into n parts and returns it as an array of queues.
    // The original queue still exists.
    
    // The user must pay attention to mark the original queue for the garbage
    // collector. ie. set pointer to null.
    public SplittableSinglyLinkedQueue<T>[] split(int n){
        SplittableSinglyLinkedQueue<T>[] array = new SplittableSinglyLinkedQueue[n];
        double partitionSize = size/((double)n);
        int currentIdx = 0;
        int leftBoundary = 0;
        int rightBoundary;
        int currentPartitionSize;
        Entry partitionHead = head;
        Entry splitHere = head;
        for(int partition = 0; partition < n; partition++){
            rightBoundary = (int)Math.ceil(partitionSize*(partition + 1));
            while(currentIdx < rightBoundary){
                splitHere = splitHere.next;
                currentIdx ++;
            }
            currentPartitionSize = rightBoundary - leftBoundary;
            array[partition] = new SplittableSinglyLinkedQueue(partitionHead.next, splitHere, currentPartitionSize);
            leftBoundary = rightBoundary;
            partitionHead = splitHere;
        }
        
        return array;
    }
    
    // Takes another queue and appends it to the tail of this one.
    // The appended queue still exists by itself, and remains unchanged even if
    // the bigger queue is dequeued fully. 
    
    // The user must pay attention to mark unused queues for the garbage collector.
    public void append(SplittableSinglyLinkedQueue attachThis){
        if(attachThis.isEmpty()){
            return;
        }
        tail.next = attachThis.head.next;
        tail = attachThis.tail;
        size += attachThis.size;
    }
    
    public boolean isEmpty(){
        return (size == 0);
    }
    
    public int size(){
        return size;
    }

    @Override
    public Iterator<T> iterator() {
        return new QueueIterator();
    }
    
    // -----------------------inner classes-------------------------------------
    
    private class QueueIterator implements Iterator<T>{
        private Entry<T> currentEntry = head;

        @Override
        public boolean hasNext() {
            return (currentEntry.next != null);
            //return index < size;
        }

        @Override
        public T next() {
            T data = currentEntry.next.data;
            currentEntry = currentEntry.next;
            return data;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
        
    }
    
    private class Entry<T>{
        T data;
        Entry<T> next;
        
        Entry(T data){
            this.data = data;
        }
        Entry(){
            
        }
    }
}


