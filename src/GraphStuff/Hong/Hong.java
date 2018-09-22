/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GraphStuff.Hong;

import DataStructures.ConditionHardBarrier;
import DataStructures.Graph;
import DataStructures.SoftBarrier;
import DataStructures.SplittableSinglyLinkedQueue;
import GraphStuff.BFSFunctionInterface;
import GraphStuff.BFSRunnable;
import GraphStuff.FWBW.FWBW;
import static GraphStuff.FWBW.FWBW.DEBUG6;
import static GraphStuff.FWBW.FWBW.G;
import static GraphStuff.FWBW.FWBW.debugPrint;
import static GraphStuff.FWBW.FWBW.processRequest;
import GraphStuff.FWBW.TrimRunnable;
import GraphStuff.IterativeTrimProcessor;
import GraphStuff.MultiStep.ColoringFirstPhaseRunnable;
import GraphStuff.MultiStep.MultiStep;
import static GraphStuff.MultiStep.MultiStep.DEBUG8;
import GraphStuff.MultiStep.SimpleBwBFSFunc;
import java.util.HashMap;
import java.util.Set;


/**
 *
 * @author Benjamin
 */
public class Hong extends MultiStep{
    final boolean HONGDEBUG = false;
    
    final boolean TRIMPRIME = true; // Turns trim prime step on or off.
    final boolean TRIMPRIME2 = true;
    final boolean TRIMPRIME3 = true;
    // Minimum size of set due for coloring propagation to create a new parallel thread.
    final static int parallelWccThreshold = 20000;
    // Determines what minimum proportion of the total graph must be covered by 
    // a single before we consider it a "large" scc. 
    final double largeSccThreshold = 0.01;
    // Decides the color for vertices that have been assigned to a component.
    // They are excluded from searches.
    // final int markedColor = Integer.MAX_VALUE;
    //Set<Integer> remainingVertices;
    
    public Hong(
            Graph graph,
            int nThreads,
            Set<Integer> remainingVertices
    ){
        super(graph, nThreads, remainingVertices);
        //this.remainingVertices = remainingVertices;
    }
    
    public void runHong() throws Exception{
        // Apply iterative trimming step. trim() inherited from FWBW
        trim(IterativeTrimProcessor.class, remainingVertices, markedColor, markedColor, null, false, false);
        if(DEBUG7) debugPrint("Completed iterative trim. remainingVertices.size(): " + remainingVertices.size());
        if(remainingVertices.isEmpty()) {
            if(DEBUG4) debugPrint("Trimmed entire searchSpace");
            return;
        }
        
        // Apply parallel FW-BW (no recursion) until "large" SCC is found.
        int fwdCol = Integer.MIN_VALUE;
        Set<Integer> sccSet = applyParallelFwbw(fwdCol);
        
        // Enable the commented boolean statement when testing other Hong phases.
        while(!remainingVertices.isEmpty() && sccSet.size() < largeSccThreshold*G.vertices.length ){
            fwdCol++;
            sccSet = applyParallelFwbw(fwdCol);
        }
        
        if(DEBUG7) debugPrint("Size of last identified SCC before ending ParFWBW phase: " + sccSet.size() + 
                ". remainingVertices.size(): " + remainingVertices.size());
        
        if(remainingVertices.isEmpty()){
            debugPrint("No large SCC found. Skipped Trim', WCC, and recursive FWBW steps.");
            return;
        }
        
        if(TRIMPRIME){
        
        recountNeighbours(remainingVertices, markedColor, false);
            
        /** 
         * Trim' below, as described in the Hong et al. paper.
         */
        
        // =================================== ITERATIVE_TRIMMING ==================================
        trim(IterativeTrimProcessor.class, remainingVertices, markedColor, markedColor, null, false, false);
        if(DEBUG7) debugPrint("Completed iterative trim. remainingVertices.size(): " + remainingVertices.size());
        
        if(TRIMPRIME2){
        /**
         * the following set goes in the nextIterationSet parameter of trimAux. Here all 
         * vertices that are neighbors of vertices trimmed by TrimTwoProcessor
         * are collected. Then the iterative trim after that can use that as a starting point.
         */
        
        Set<Integer> nextIterativeTrimStartHere = setType.getClass().newInstance();
        
        //recountNeighbours(remainingVertices, markedColor, false);
        
        /**
         * Create a copy of the remainingVertices set, to avoid 
         * ConcurrentModificationException.
         */
        Set<Integer> trimTwoSpace = setType.getClass().newInstance();
        for(int i: remainingVertices) trimTwoSpace.add(i);
        // Single trim iteration for size 2 components. =========== SIZE_2_COMPONENT_TRIMMING ================
        trimAux(
                TrimTwoProcessor.class, remainingVertices, trimTwoSpace, 
                nextIterativeTrimStartHere, markedColor, markedColor, false, false
        );
        
        if(DEBUG7) debugPrint("Completed Trim2. remainingVertices.size(): " + remainingVertices.size() +
                ". Size of trimSpace for next iterative trimming step: " + nextIterativeTrimStartHere.size());
        
        if(TRIMPRIME3){
        /** ========================================= ITERATIVE_TRIMMING ================================
         * Repeat another iterative trim, but use trimSpace as a starting point
         * for the first iteration. This set was nicely provided to us by TrimTwoRunnable.
         * Below is some copied code from FWBW.trim(). The method it is copied from makes a new 
         * trimSpace on the spot, but we need to use an already existing set, 
         * and therefore cannot call the method directly.
         */
            
        trim(IterativeTrimProcessor.class, remainingVertices, markedColor, markedColor, nextIterativeTrimStartHere, false, false);
        
        /*//////////////////////////////////////// Start old way
        Set<Integer> nextIterationSet = setType.getClass().newInstance(); 
        while(!trimSpace.isEmpty()){
            trimAux(
                    HongTrimRunnable.class, remainingVertices, trimSpace, 
                    nextIterationSet, markedColor, markedColor
            );
            trimSpace = nextIterationSet;
            nextIterationSet = setType.getClass().newInstance();
            if(DEBUG2 || DEBUG3) debugPrint("finished trim iteration, next set size: " + trimSpace.size());
        }//*////////////////////////////////////// End old way
        
        if(DEBUG7) debugPrint("Completed iterative trim. remainingVertices.size(): " + remainingVertices.size());
        
        } // end if(TRIMPRIME3)
        } // end if(TRIMPRIME2)
        } // end if(TRIMPRIME)
        
        // WCC decomposition
        /** 
         * Below is some copied and edited code from MultiStep.coloring(). Since 
         * WCCRunnable extends ColoringFirstPhaseRunnable, it is invoked and 
         * parallelized in exactly the same way.
         */
        // Set the color equal to the vertex identifier.
        for(int i: remainingVertices){
            G.vertices[i].color = i;
        }
        
        if(HONGDEBUG) System.out.println("before color propagation.");
        // Populate a queue for parallel iteration.
        SplittableSinglyLinkedQueue<Integer> coloringSpace = new SplittableSinglyLinkedQueue();
        for(int i: remainingVertices){
            coloringSpace.enq(i);
        }
        
        boolean[] visited = new boolean[G.vertices.length];
        
        // coloring 1st phase loop
        while(!coloringSpace.isEmpty()){
            if(DEBUG8) debugPrint("auxSpace size: " + coloringSpace.size());
            SplittableSinglyLinkedQueue<Integer>[] queues = coloringAux(WCCRunnable.class, coloringSpace, visited, parallelWccThreshold); // TODO: rework coloringAux arguments to accept a Class type.
            coloringSpace = mergeAllQueues(queues);
        }
        // End copied code
        if(HONGDEBUG) System.out.println("after color propagation.");
        
        /** 
         * Sweep over the entire remainingVertices set, and populate a
         * HashMap<Integer, HashSet<Integer>> where each color is mapped onto a 
         * set of vertices that all have that color.
         */
        
        HashMap<Integer,Set<Integer>> map = new HashMap<>();
        assembleWccSets(map);
        
        if(HONGDEBUG) System.out.println("after assembleWccSets.");
        
        // Add all unique keys in the map (WCC colors) to a queue.
        SplittableSinglyLinkedQueue<Integer> WCCs = new SplittableSinglyLinkedQueue();
        for(int wcc: map.keySet()){
            WCCs.enq(wcc);
        }
        
        if(HONGDEBUG) System.out.println("after populating WCCs queue.");
        
        // Call normal recursive FWBW in parallel on each WCC.
        createRecFWBWRunnables(WCCs, map);
    }
    
    /**
     * The following method is almost identical to MultiStep.coloringAux(). Except
     * differs in that it creates WCCRunnables instead of ColoringFirstPhaseRunnables.
     * TODO: modify the similar method to take in a Class, and then use getClass.newInstance();
     * and use a setup() method as pseudo constructor.
     * @param coloringSpace
     * @param visited
     * @return 
     */
    
    
    Set<Integer> applyParallelFwbw(int fwdCol){
        
        // pick a starting vertex
        int initialVertex = 0;
        for(int i: remainingVertices){
            initialVertex = i;
            break;
        }
        
        SplittableSinglyLinkedQueue<Integer> q = new SplittableSinglyLinkedQueue();
        q.enq(initialVertex);
        G.vertices[initialVertex].color = fwdCol;
        
        BFSFunctionInterface callbackObj = new HongSimpleFwBFSFunc(q, markedColor, fwdCol);
        
        SoftBarrier sBarrier = new SoftBarrier();
        BFSRunnable r = new BFSRunnable(callbackObj, q, sBarrier, false);
        
        r.run();
        
        Set<Integer> sccSet = null;
        try{
            sccSet = FWBW.setType.getClass().newInstance();
        } catch(Exception e){System.out.println(e);System.exit(0);}
        
        G.vertices[initialVertex].color = markedColor;
        sccSet.add(initialVertex);
        q.enq(initialVertex);
        remainingVertices.remove(initialVertex);
        
        BFSFunctionInterface callbackObj2 = new SimpleBwBFSFunc(q, sccSet, fwdCol, markedColor, remainingVertices);
        BFSRunnable r2 = new BFSRunnable(callbackObj2, q, sBarrier, false);
        
        r2.run();
        
        if(DEBUG7) {
            //if(sccSet.size() > 1)
            FWBW.debugPrint("SCC found by FW-BW pass has size " + sccSet.size());
        }
        outputSCC(sccSet);
        return sccSet;
    }
    
    int vertexCol;
    void assembleWccSets(HashMap<Integer, Set<Integer>> map) throws InstantiationException, IllegalAccessException{
        for(int elem: remainingVertices){
            vertexCol = G.vertices[elem].color;
            if(!map.containsKey(vertexCol)){
                map.put(vertexCol, setType.getClass().newInstance());
            }
            map.get(vertexCol).add(elem);
        }
    }
    
    private void createRecFWBWRunnables(
            SplittableSinglyLinkedQueue<Integer> WCCs,
            HashMap<Integer, Set<Integer>> map
    ){
        int extraThreadsGranted = processRequest(threadPoolSize);
        SoftBarrier sBarrier = new SoftBarrier();
        
        Thread[] threads = new Thread[extraThreadsGranted];
        for(int i = 0; i < threads.length; i++){
            RecFWBWRunnable r = new RecFWBWRunnable(WCCs, map, sBarrier, true);
            threads[i] = new Thread(r); 
        }
        
        RecFWBWRunnable j = new RecFWBWRunnable(WCCs, map, sBarrier, false);
        
        if(DEBUG6) {
            if(extraThreadsGranted > 0) debugPrint("Starting " + extraThreadsGranted + " worker RecFWBWRunnables.");
        }
        
        for(Thread thread: threads){
            sBarrier.workerCheckIn();
            thread.start();
        }
        j.run();
    }
}
