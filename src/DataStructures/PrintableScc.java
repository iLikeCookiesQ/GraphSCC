/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package DataStructures;

import java.util.Comparator;

/**
 * Data storage class that groups an Iterable with its size.
 * @author Benjamin
 */
public class PrintableScc implements Comparator<PrintableScc>{
    private final int size;
    private final Iterable<Integer> iterable;
    
    public PrintableScc(int size, Iterable<Integer> iterable){
        this.size = size;
        this.iterable = iterable;
    }
    
    public int getSize(){
        return size;
    }
    
    public Iterable<Integer> getIterable(){
        return iterable;
    }

    @Override
    public int compare(PrintableScc o1, PrintableScc o2) {
        return o2.size - o1.size;
    }
}
