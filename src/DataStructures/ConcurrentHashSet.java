/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package DataStructures;

import java.util.HashSet;

/**
 * Wraps the HashSet class and makes it thread safe for add and remove operations.
 * @author Benjamin
 */
public class ConcurrentHashSet<T> extends HashSet<T>{
    private final Object lock = new Object();
    
    @Override
    public boolean add(T elem){
        synchronized(lock){
            return super.add(elem);
        }
    }
    
    @Override
    public boolean remove(Object O){
        synchronized(lock){
            return super.remove(O);
        }
    }
}
