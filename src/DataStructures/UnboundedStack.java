/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package DataStructures;

import java.util.Iterator;

/**
 * Implements a stack using doubly linked list logic.
 * @author Benjamin
 */
public class UnboundedStack<T> implements Iterable<T>{
    private Entry<T> head;
    private int size;
    
    public UnboundedStack(){
        head = new Entry();
        size = 0;
    }
    
    public void push(T data){
        head.data = data;
        head.next = new Entry();
        head.next.previous = head;
        head = head.next;
        size++;
    }
    
    public T pop(){
        head = head.previous;
        head.next = null;
        size--;
        return head.data;
    }
    
    public int size(){
        return size;
    }

    @Override
    public Iterator iterator() {
        return new UnboundedStackIterator();
    }
    
    private class Entry<T>{
        T data;
        Entry<T> next;
        Entry<T> previous;
        
        Entry(T data){
            this.data = data;
        }
        Entry(){
            
        }
    }
    
    private class UnboundedStackIterator implements Iterator<T>{
        private Entry<T> currentEntry = head;
        
        @Override
        public boolean hasNext() {
            return (currentEntry.previous != null);
        }

        @Override
        public T next() {
            currentEntry = currentEntry.previous;
            return currentEntry.data;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        } 
    }
}
