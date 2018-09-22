/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package DataStructures;

import DataStructures.ParallelizationSupport.ElementProcessor;
import DataStructures.ParallelizationSupport.ParallelIterator;
import GraphStuff.MultiStep.TopNCollectorProcessor;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
/**
 * This class exists for testing and experimentation purposes only.
 * Doesn't implement any function.
 * @author Benjamin
 */
public class TopLevel {
    public static void main(String[] args){
        /*
        System.out.println("Size: " + q.size() + "\n");
        SplittableSinglyLinkedQueue<Integer> latterHalf = q.split();
        System.out.println("Size: " + q.size() + "\n");
        System.out.println("Size: " + latterHalf.size() + "\n");
        printQueue(q);
        printQueue(latterHalf);
        System.out.println("Size: " + q.size() + "\n");
        System.out.println("Size: " + latterHalf.size() + "\n");
        q.enq(1);
        q.enq(2);
        System.out.println("Size: " + q.size() + "\n");
        latterHalf.enq(3);
        latterHalf.enq(4);
        latterHalf.enq(5);
        System.out.println("Size: " + latterHalf.size() + "\n");
        q.append(latterHalf);
        System.out.println("Size: " + q.size() + "\n");
        System.out.println("Size: " + latterHalf.size() + "\n");
        printQueue(q);
        printQueue(latterHalf);
        */
        
        /*
        SplittableSinglyLinkedQueue<Integer> q = new SplittableSinglyLinkedQueue();
        int nq = 7;
        for(int i = 0; i < 31; i++){
            q.enq(i);
        }
        
        SplittableSinglyLinkedQueue<Integer>[] array = q.split(nq);
        for(int i = 0; i < nq; i++){
            System.out.println(array[i].size());
        }
        
        System.out.println("");
        
        for(int i = 0; i < nq; i++){
            array[i].enq(nq);
            System.out.println(array[i].size());
        }
        for(int i = 0; i < nq; i++){
            printQueue(array[i]);
        }
        */
        
        /*
        System.out.println("Hello???");
        SplittableSinglyLinkedQueue<Integer> q = new SplittableSinglyLinkedQueue();
        for(int i = 0; i < 10; i++){
            q.enq(i);
            
        }
        System.out.println("Hello???");
        for(int i: q){
            System.out.println(i);
        }
        
        printQueue(q);
        
        for(int i: q){
            System.out.println(i);
        }
        
        q.enq(1);
        
        for(int i: q){
            System.out.println(i);
        }*/
        
        /*
        SplittableSinglyLinkedQueue<Integer> q = new SplittableSinglyLinkedQueue();
        q.enq(1);
        iterateQueue(q);
        SplittableSinglyLinkedQueue<Integer> q2 = new SplittableSinglyLinkedQueue();
        //q2.enq(2);
        q.append(q2);
        iterateQueue(q);
        SplittableSinglyLinkedQueue<Integer> q3 = new SplittableSinglyLinkedQueue();
        q3.enq(3);
        q.append(q3);
        iterateQueue(q);
        SplittableSinglyLinkedQueue<Integer> q4 = new SplittableSinglyLinkedQueue();
        q4.enq(4);
        q.append(q4);
        iterateQueue(q);
        */
        
        /*
        final boolean ITERATE = true;
        
        SplittableSinglyLinkedQueue<Integer> q = new SplittableSinglyLinkedQueue();
        for(int i = 0; i < 1; i++){
            q.enq(i);
        }
        iterateQueue(q);
        SplittableSinglyLinkedQueue<Integer> q2 = q.split();
        
        if(ITERATE)iterateQueue(q); else printQueue(q);
        if(ITERATE)iterateQueue(q2);else printQueue(q2);
        
        q.deq();
        q2.deq();
        iterateQueue(q);
        printQueue(q);
        iterateQueue(q2);
        printQueue(q2);
        //*/
        
        /*
        UnboundedStack<Integer> stack = new UnboundedStack();
        for(int i = 0; i < 10; i++){
            stack.push(i);
        }
        
        for(int elem: stack){
            System.out.print(elem + " ");
        }
        System.out.print("\n");
        while(stack.size() != 0){
            System.out.println(stack.pop());
            stack.push(100);
            System.out.println(stack.pop());
        }
        stack.push(1640);
        for(int elem: stack){
            System.out.print(elem + " ");
        }
        System.out.println(stack.pop());
        //*/
        
        /*
        ArrayList<Integer> list = new ArrayList(10);
        list.add(4);
        list.add(7);
        list.add(5);
        list.add(6);
        list.add(2);
        list.add(9);
        list.add(3);
        list.add(8);
        list.add(1);
        list.add(0);
        
        TopNCollector<Integer> collector = new TopNCollector(list , list.size());
        for(int elem: collector.getTopN()){
            System.out.println(elem);
        }*/
        
        //testComparator(1);
        //testComparator(2);
        //testComparator(3);
        
        /**
         * the code below tests the ParallelIterator class.
         */
        /*
        final int size = 21;
        TreeSet<Integer> set = new TreeSet();
        for(int i = 0; i < size; i++) set.add(i);
        ElementProcessor<Integer> callbackObject = new TestProcessor<>(0);
        ParallelIterator<Integer> pi = new ParallelIterator(set, size, 4, callbackObject);
        pi.iterate();//*/
        
        /*
        TreeSet<TwoInts> set = new TreeSet<>(new TwoInts());
        set.add(new TwoInts(0, 0));
        set.add(new TwoInts(0, 0));
        set.add(new TwoInts(1, 0));
        set.add(new TwoInts(0, 1));
        set.add(new TwoInts(1, 1));//*/
        
        /**
         * Testing the TopNCollectorCallback class below
         */
        
        /*
        final int size = 400;
        final int nThreads = 1;
        final int topN = 7;
        Graph G = new Graph(size);
        HashSet<Integer> all = new HashSet();
        
        for(int i = 0; i < size; i++){
            G.vertices[i] = new Vertex(i, size - i);
            all.add(i);
        }
        
        TreeSet<TwoInts>[] sets = new TreeSet[nThreads];
        for(int i = 0; i < nThreads; i++){
            sets[i] = new TreeSet(new TwoInts());
        }
        
        ElementProcessor<Integer> callbackObj = new TopNCollectorProcessor(sets, topN, G);
        ParallelIterator<Integer> pi = new ParallelIterator(all, all.size(), nThreads, callbackObj);
        pi.iterate();
        
        for(int i = 0; i < nThreads; i++){
            System.out.println("Thread " + i);
            printSet(sets[i], G);
            System.out.println("\n");
        }//*/
        
        SplittableSinglyLinkedQueue<Integer> queue = new SplittableSinglyLinkedQueue();
        for(int i = 0; i < 5; i++) queue.enq(i);
        iterateQueue(queue);
        iterateQueue(queue);
    }
    
    
    static void printSet(TreeSet<TwoInts> set, Graph G){
        for(TwoInts ti: set){
            System.out.print("vertexId: " + ti.vertexId + "\t\tstatistic: " + ti.statistic);
            System.out.print("\t\tnIn: " + G.vertices[ti.vertexId].nIn);
            System.out.println("\t\tnOut: " + G.vertices[ti.vertexId].nOut);
        }
    }
    
    static void iterateQueue(SplittableSinglyLinkedQueue<Integer> q){
        for(int i: q){
            System.out.print(i + " ");
        }
        System.out.print("\n");
    }
    
    static void printQueue(SplittableSinglyLinkedQueue q){
        if(q.isEmpty()) System.out.println("error: printing empty queue.");
        while(!q.isEmpty()){
            System.out.print(q.deq() + " ");
        }
        System.out.print("\n");
    }
    
    static void testComparator(Comparable<Integer> x){
        System.out.print(x.compareTo(3) + " ");
    }
    
    /**
     * This helps testing the ParallelIterator class by supplying a basic impementation
     * for the ElementProcessor interface.
     * @param <T> 
     */
    private static class TestProcessor<T> implements ElementProcessor<T>{
        public TestProcessor(int threadId){
            //super(threadId);
        }
        @Override
        public void processElement(T element) {
            //synchronized(this){
                System.out.println(Thread.currentThread().getName() + "\t\t" + element);
            //}
        }

        @Override
        public ElementProcessor<T> clone(int cloneId) {
            return new TestProcessor(cloneId);
        }
        
    }
}
