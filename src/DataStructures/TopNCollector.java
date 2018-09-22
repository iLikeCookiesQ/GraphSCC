/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package DataStructures;

import java.util.Comparator;
import java.util.Iterator;
import java.util.TreeSet;


/**
 * This class collects top top N elements from an Iterable of Comparables.
 * This class is currently not in use by any graph algorithm in this project.
 * @author Benjamin
 * @param <T> The type of object modeled by Iterable.
 */
public class TopNCollector<T extends Comparable<T>> {
    final boolean DEBUG = false;
    
    private final Iterable<T> iterable;
    private final int topLength;
    //private final T zeroElem; // decides what the 0 is for the Comparable
    
    private final TreeSet<T> topN;
    
    public TopNCollector(
            Iterable<T> iterable, 
            int topLength
            //, T zeroElem
    ){
        this.iterable = iterable;
        this.topLength = topLength;
        //this.zeroElem = zeroElem;
        
        this.topN = new TreeSet(new TComparator());
    }
    
    public TreeSet<T> getTopN(){
        //if(DEBUG) System.out.println("Entered getTopN()");
        Iterator<T> iterator = iterable.iterator();
       // if(DEBUG) System.out.println("iterator is null? " + (iterator == null));
        int addedCount = 0;
        while(iterator.hasNext()){
            if(addedCount == topLength) break;
            topN.add(iterator.next());
            addedCount++;
        }
        //if(DEBUG) System.out.println("addedCount and topLength " + addedCount + "" + topLength);
        
        T elem;
        T lowest = topN.first();
        while(iterator.hasNext()){
            //if(DEBUG) System.out.println("iterator.hasNext() passed.");
            elem = iterator.next();
            //if(DEBUG) System.out.println("elem.compareTo(lowest)" + elem.compareTo(lowest));
            if(elem.compareTo(lowest) > 0){
                topN.remove(lowest);
                topN.add(elem);
                lowest = topN.first();
            }
        }
        return topN;
    }
    
    private class TComparator implements Comparator<T>{
        @Override
        public int compare(T a, T b) {
            return a.compareTo(b);
        }
    }
}
