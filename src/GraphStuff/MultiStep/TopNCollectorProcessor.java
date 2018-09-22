/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GraphStuff.MultiStep;


import DataStructures.Graph;
import DataStructures.ParallelizationSupport.ElementProcessor;
import DataStructures.TwoInts;
import java.util.Comparator;
import java.util.TreeSet;

/**
 * Callback function for the parallelization of the getTopN method in MultiStep
 * @author Benjamin
 */
public class TopNCollectorProcessor implements ElementProcessor<Integer>{
    
    final TreeSet<TwoInts>[] sets; // Each thread writes to their own set
    final TreeSet<TwoInts> set;
    final int topLength;
    final Graph G;
    
    public TopNCollectorProcessor(TreeSet<TwoInts>[] sets, int topLength, Graph G){
        //super(0);
        this.sets = sets;
        this.set = sets[0];
        this.topLength = topLength;
        this.G = G;
    }
    
    private TopNCollectorProcessor(TreeSet<TwoInts> set, int topLength, Graph G){
        //super(threadId);
        this.sets = null;
        this.set = set;
        this.topLength = topLength;
        this.G = G;
    }
    
    private TwoInts currentTwoInts;
    private TwoInts lowest;
    @Override
    public void processElement(Integer element) {
        //System.out.println("threadId: " + threadId + " processing element: " + element);
                
        currentTwoInts = getProduct(element);
        if(set.size() < topLength){
            set.add(currentTwoInts);
        } else{
            lowest = set.first();
            if(currentTwoInts.compareTo(lowest) > 0){
                set.remove(lowest);
                set.add(currentTwoInts);
            }
        }
    }
    
    private int product;
    /**
     * Creates a TwoInts object holding a vertex identifier and its product of 
     * in- and out degrees.
     * @param vertexId: The integer id of a vertex.
     * @return: A TwoInts object holding the vertexId and the product.
     */
    TwoInts getProduct(int vertexId){
        product = G.vertices[vertexId].nIn.get() * G.vertices[vertexId].nOut.get();
        return new TwoInts(vertexId, product);
    }

    @Override
    public ElementProcessor<Integer> clone(int cloneId) {
        return new TopNCollectorProcessor(sets[cloneId], topLength, G);
    }
}
