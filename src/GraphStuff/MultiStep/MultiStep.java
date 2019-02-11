/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GraphStuff.MultiStep;

import GraphStuff.FWBW.FWBW;
import java.util.concurrent.ConcurrentSkipListSet;
import DataStructures.ConditionHardBarrier;
import DataStructures.Graph;
import DataStructures.ParallelizationSupport.ElementProcessor;
import DataStructures.ParallelizationSupport.ParallelIterator;
import DataStructures.SoftBarrier;
import DataStructures.SplittableSinglyLinkedQueue;
import DataStructures.Tagging.Colorer;
import DataStructures.UnboundedStack;
import DataStructures.Vertex;
import DataStructures.TwoInts;
import GraphStuff.BFSFunctionInterface;
import GraphStuff.BFSRunnable;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 
 * 
 * @author Benjamin
 */
public class MultiStep extends FWBW{
    public static final boolean DEBUG7 = true; // high level progress updates
    public static final boolean DEBUG8 = false; // spam every detail
    
    /*
     * Apply coloring iterations until fewer than this number of vertices remaind
     * in the graph. Then finish the rest of the graph with Tarjan
     */
    int nCutoff;
    /**
     * The following few "threshold" variables dictate the minimum size of a set
     * before we start a an extra thread to help iterate over it.
     */
    // Threshold for size of single coloring propagation step.
    final static int parallelColoringThreshold = 2000;
    // Threshold for size of set whereof the top n highest in and outdegree vertices
    // need to be obtained.
    final static int parallelTopNThreshold = 10000;
    // Threshold for size of set for color initialization.
    final static int parallelColoringInitializationThreshold = 25000;
    /*
     * Create a list of the top topN vertices with their product of in and out
     * degrees. Then assign these vertices the highest colors in the graph at
     * the start of a color propagation step. (ColoringFirstPhaseRunnable)
     *
     * Set to 0 to disable.
     */
    final static int topSize = 25;
    TreeSet<TwoInts> topN;
    /*
     * decides how many threads we leave for parallelization of BFS. 
     * Line 9 of alg 2, in the MultiStep paper. This number divides the total
     * amount of threads available for this part of the pseudocode. Higher means,
     * fewer threads available for parallelized emptying of the roots list, but
     * more threads available for parallelization of BFSs from the roots.
     * Recommended to leave at 1, as big BFSs will have threads available when
     * other ColoringSecondPhaseRunnables finish regardless.
     */
    final static int coloringBFSThreadRatio = 1;
    
    public final int markedColor = Integer.MAX_VALUE;
    
    
    public static Set<Integer> remainingVertices;
    
    private int tarjanIndexCounter;
    private HashMap<Integer, TarjanData> tarjanFields;
    private UnboundedStack<Integer> stack;
    
    public final static AtomicInteger trimmedCount = new AtomicInteger(0);
    
    public MultiStep(
            Graph graph,
            int nThreads,
            Set<Integer> remainingVertices
    ){
        
        super(graph, nThreads);
        MultiStep.remainingVertices = remainingVertices;
        this.nCutoff = G.vertices.length/20;
        //nCutoff = 0;
    }
    
    public void runMultiStep(){
        if(FWBW.TRIM)simpleTrim(); // sets color of trimmed elements to Int_MAX
        if(DEBUG7) debugPrint(trimmedCount.get() + " vertices trimmed in simple trim step.");
        
        if(remainingVertices.isEmpty()) return;
        // Update neighbor counts before picking highest product vertex
        recountNeighbours(remainingVertices, 0, true);
        
        //int crowdedVertex = getTopN(1).first().vertexId;
        int crowdedVertex = getCrowdedVertex();
        
        if(DEBUG7) {
            int product = G.vertices[crowdedVertex].nIn.get()*G.vertices[crowdedVertex].nOut.get();
            debugPrint("crowdedVertex: " + crowdedVertex + " has product "
                + product);//*/
        }
        
        int oldSize = remainingVertices.size();
        Set<Integer> sccSet = singleFwBwPass(crowdedVertex); // sets color of SCC vertices to Int_MAX
                                                             // and its out shadow to Int_MAX - 1
        
        // Update neighborcounts in case of doing TopN logic.
        if(topSize > 0){
            if(sccSet.size() > 0.5*oldSize){
                recountNeighbours(remainingVertices, Integer.MAX_VALUE, false);
            } else {
                updateSccNeighbors(sccSet, sccSet.size()); 
            }
        }
        
        topN = getTopN(topSize);
        // coloring sets the color of assigned vertices to Int_MAX
        while(remainingVertices.size() > nCutoff){
            coloring();
        }
        
        // Set color of remaining vertices back to 0 for the Tarjan algorithm
        // to recognize what should be processed.
        for(int elem: remainingVertices) G.vertices[elem].color = 0;
        
        tarjanFields = new HashMap();
        for(int elem: remainingVertices) tarjanFields.put(elem, new TarjanData(0, 0, false));
        stack = new UnboundedStack();
        
        while(remainingVertices.size() > 0){
            if(DEBUG8) debugPrint("Running Tarjan. Vertices remaining: " + remainingVertices.size());
            // ghetto way to select a random vertex
            int initialVertex = 0;
            for(int i: remainingVertices){
                initialVertex = i;
                break;
            }
            tarjanIndexCounter = 0;
            // Assignment color is 1
            tarjan(initialVertex, 0, markedColor);
        }//*/
        
    }
    
    /**
     * Returns the vertex identifier of the vertex with the highest product of 
     * in and out degrees.
     * @return 
     */
    int getCrowdedVertex(){
        int idOfHighest = 0;
        int valueOfHighest = 0;
        
        Vertex auxVertex;
        int productOfCurrent;
        for(int i = 0; i < G.vertices.length; i++){
            auxVertex = G.vertices[i];
            productOfCurrent = auxVertex.nIn.get()*auxVertex.nOut.get();
            if(productOfCurrent > valueOfHighest){
                idOfHighest = i;
                valueOfHighest = productOfCurrent;
            }
        }
        
        if(DEBUG7) debugPrint("valueOfHighest: " + valueOfHighest);
        return idOfHighest;
    }
    
    /**
     * Gets the top n vertices with the highest product of in and out degrees.
     * @param topLength defines the size of the top. 
     * @return a TreeSet of size topLenght containing vertex identifiers.
     * 
     */
    private TreeSet<TwoInts> getTopN(int topLength){
        if(topLength == 0) return new TreeSet();
        
        // Request an appropriate number of threads. This also reserves the thread slots.
        int extraThreadsGranted = decideNThreads(remainingVertices.size(), parallelTopNThreshold);
        int nThreads = extraThreadsGranted + 1;
        
        // Create an array of sets where the threads will collect their local top n.
        TreeSet<TwoInts>[] topSets = new TreeSet[nThreads];
        for(int i = 0; i < nThreads; i++){
            /**
             * Instantiate the TreeSets with the Comparator implementer TwoInts.
             * This tells the TreeSets how to decide which element has a higher value
             * and thus facilitates sorting.
             */
            topSets[i] = new TreeSet(new TwoInts());
        }
        
        // Define our callback object and setup the ParallelIterator.
        ElementProcessor<Integer> callbackObj = new TopNCollectorProcessor(topSets, topSize, G);
        ParallelIterator<Integer> pi = new ParallelIterator(remainingVertices, remainingVertices.size(), nThreads, callbackObj);
        
        if(DEBUG6){
            if(extraThreadsGranted > 0){
                debugPrint("Starting " + extraThreadsGranted + " worker TopNCollectorProcessor threads.");
            }
        }
        // Launch the parallel iteration.
        pi.iterate();
        
        // Free up the thread slots.
        for(int i = 0; i < extraThreadsGranted; i++) checkOutThread();
        
        /**
         * We now have an array of sets, where each set is (most likely) of size
         * topSize. So to get the actual topSize highest elements we need to iterate
         * through the sets and collect the topSize highest elements. We do that
         * by iteration through sets 1 through nThreads and comparing each of their
         * elements to the lowest in set 0. If so, we add it to set 0 and evict the 
         * lowest. Set 0 will this way contain the final topSize highest elements.
         */
        TwoInts lowest;
        for(int i = 1; i < nThreads; i++){
            for(TwoInts ti: topSets[i]){
                lowest = topSets[0].first();
                if(ti.compareTo(lowest) > 0){
                    topSets[0].remove(lowest);
                    topSets[0].add(ti);
                }
            }
        }
        
        return topSets[0];
        
        /*// Old code.
        if(topLength == 0) return new TreeSet();
        
        TreeSet<TwoInts> topSet = new TreeSet(new TwoInts());
        Iterator<Integer> iterator = remainingVertices.iterator();
        
        int currentElem;
        int product;
        Vertex currentVertex;
        
        int addedCount = 0;
        while(iterator.hasNext()){
            if(addedCount == topLength) break;
            currentElem = iterator.next();
            currentVertex = G.vertices[currentElem];
            product = currentVertex.nIn*currentVertex.nOut;
            topSet.add(new TwoInts(currentElem, product));
            addedCount++;
        }
        
        TwoInts lowest = topSet.first();
        while(iterator.hasNext()){
            currentElem = iterator.next();
            currentVertex = G.vertices[currentElem];
            product = currentVertex.nIn*currentVertex.nOut;
            if(product > lowest.statistic){
                topSet.remove(lowest);
                topSet.add(new TwoInts(currentElem, product));
                lowest = topSet.first();
            }
        }
        
        return topSet;//*/
    }
    
    // top level coloring method
    void coloring(){
        // set the color equal to the vertex identifier. (Except for topN vertices)
        initializeColors(remainingVertices);
        // Fill a queue for parallel iteration
        SplittableSinglyLinkedQueue<Integer> auxSpace = new SplittableSinglyLinkedQueue();
        for(int i: remainingVertices){
            auxSpace.enq(i);
        }
        
        boolean[] visited = new boolean[G.vertices.length];
        
        // coloring 1st phase loop
        while(!auxSpace.isEmpty()){
            try {
                if(DEBUG8) debugPrint("auxSpace size: " + auxSpace.size());
                SplittableSinglyLinkedQueue<Integer>[] queues = coloringAux(ColoringFirstPhaseRunnable.class, auxSpace, visited, parallelColoringThreshold);
                auxSpace = mergeAllQueues(queues);
            } catch (Exception e) {
                System.out.println(e);
                System.exit(0);
            }
        }
        
        // 1st coloring phase done, now find the roots whose color did not change
        
        // first add special roots to the specialRoots queue. Special roots are
        // vertices that were in the topN that retained their initialized color.
        SplittableSinglyLinkedQueue<TwoInts> specialRoots = new SplittableSinglyLinkedQueue();
        for(TwoInts ti: topN){
            if(G.vertices[ti.vertexId].color == ti.statistic){
                specialRoots.enq(ti);
            }
        }
        if(DEBUG8) debugPrint("specialRoots.size(): " + specialRoots.size());
        
        // add normal roots to roots queue
        SplittableSinglyLinkedQueue<Integer> roots = new SplittableSinglyLinkedQueue();
        for(int i: remainingVertices){
            if(G.vertices[i].color == i){
                roots.enq(i);
            }
        }
        if(DEBUG8) debugPrint("roots.size(): " + roots.size());
        
        // Start parallel ColoringSecondPhaseRunnable s to do backwards searches from the roots.
        SoftBarrier sBarrier = new SoftBarrier();
        int extraThreadsGranted = processRequest(FWBW.threadPoolSize/coloringBFSThreadRatio - 1);
        //int extraThreadsGranted = processRequest(1);
        
        Thread[] threads = new Thread[extraThreadsGranted];
        for(int i = 0; i < threads.length; i++){
            // Use G.vertices.length as the color to mark a vertex as part of an SCC
            ColoringSecondPhaseRunnable r = new ColoringSecondPhaseRunnable(specialRoots, roots, markedColor, sBarrier, true);
            threads[i] = new Thread(r);
        }
        ColoringSecondPhaseRunnable r = new ColoringSecondPhaseRunnable(specialRoots, roots, markedColor, sBarrier, false);
        if(DEBUG6) {
            if(extraThreadsGranted > 0) debugPrint(
                    "Starting " + extraThreadsGranted + 
                    " worker ColoringSecondPhaseRunnables."
            );
        }
        for(Thread thread: threads){
            sBarrier.workerCheckIn();
            thread.start();
        }
        r.run();
        
        // Clear the topN set such that later coloring iterations skip over it.
        topN.clear();
    }
    
    /**
     * Assigns appropriate starting colors to vertices before coloring iteration
     * @param coloringSpace The set of vertices whose colors need to be initialized.
     */
    public void initializeColors(Set<Integer> coloringSpace){
        // First, naively set all colors equal to vertex identifiers.
        
        /*  Below template code for parallelizing normal color initialization.
        int extraThreadsGranted = decideNThreads(coloringSpace, parallelColoringInitializationThreshold);
        ElementProcessor<Integer> callbackObj = new ColorInitializationCallback(G); // change this line
        ParallelIterator<Integer> pi = new ParallelIterator(coloringSpace, coloringSpace.size(), extraThreadsGranted + 1, callbackObj);
        if(DEBUG6){
            if(extraThreadsGranted > 0){
                debugPrint("Starting " + extraThreadsGranted + " worker ColorInitialization threads.");
            }
        }
        pi.iterate();
        for(int i = 0; i < extraThreadsGranted; i++) checkOutThread();
        // End template code. */
        
        // Below non parallel color initialization.
        for(int i: coloringSpace){
            G.vertices[i].color = i;
        }//*/
        
        // Lastly give the vertices in topN the highest colors.
        // Give the lowest of topN the color G.vertices.length + 1.
        // G.vertices.length itself must not be used because ColoringSecondPhaseRunnable
        // uses this color as its sccCol.
        int counter = 1;
        int specialCol;
        for(TwoInts ti: topN){
            specialCol = G.vertices.length + counter;
            G.vertices[ti.vertexId].color = specialCol;
            // Overwrite product of in and out degree in statistic with the initialized color.
            // This is needed later in ColoringSecondPhaseRunnable to pass the correct
            // srchSpaceCol to SimpleBwBFSFunc.
            ti.statistic = specialCol;
            counter++;
        }
    }
    
    /**
     * Deals with creating multiple threads to iterate over auxSpace from the
     * calling method.
     * @param coloringSpace The set of vertices whose colors need to be propagated 
     * this propagation iteration. 
     * @param visited
     * @param parallelThreshold: minimum size of coloringSpace to create parallel thread
     * for parallel iteration.
     * @return An array of sets, where each thread filled its own set. Each element
     * placed in a set will be processed next iteration.
     */
    public <T extends Colorer> SplittableSinglyLinkedQueue<Integer>[] coloringAux(
            Class<T> klass,
            SplittableSinglyLinkedQueue<Integer> coloringSpace,
            boolean[] visited,
            int parallelThreshold
    ) throws InstantiationException, IllegalAccessException{    
        int extraThreadsGranted = decideNThreads(coloringSpace.size(), parallelThreshold);
        
        /*
        int extraThreadsWanted  = (int)Math.ceil(coloringSpace.size()/(double)parallelColoringThreshold) - 1;
        int extraThreadsGranted = processRequest(extraThreadsWanted);//*/
        
        int totalThreads = extraThreadsGranted + 1;
        SplittableSinglyLinkedQueue<Integer>[] queues = new SplittableSinglyLinkedQueue[totalThreads];
        ConditionHardBarrier hBarrier = new ConditionHardBarrier(totalThreads);
        
        Thread[] threads = new Thread[extraThreadsGranted];
        for(int i = 0; i < threads.length; i++){
            Colorer j = klass.newInstance();
            j.setup(coloringSpace, queues, i, hBarrier, visited, true, G);
            threads[i] = new Thread(j);
        }
        Colorer r = klass.newInstance();
        r.setup(coloringSpace, queues, extraThreadsGranted, hBarrier, visited, false, G);
        
        if(DEBUG6) {
            if(extraThreadsGranted > 0) debugPrint("Starting " + 
                    extraThreadsGranted + " worker instances of " + klass.getName());
        }
        for(Thread thread: threads){
            thread.start();
        }
        r.run();
        
        return queues;
    }
    
    /*
    public SplittableSinglyLinkedQueue<Integer> mergeAllQueues(SplittableSinglyLinkedQueue<Integer>[] queues){
        SplittableSinglyLinkedQueue<Integer> returnValue = new SplittableSinglyLinkedQueue();
        for(int i = 0; i < queues.length; i++){
            if(DEBUG8) debugPrint("appending q of size " + queues[i].size());
            returnValue.append(queues[i]);
        }
        return returnValue;
    }//*/
    
    void simpleTrim(){
        SplittableSinglyLinkedQueue<Integer> queue = new SplittableSinglyLinkedQueue();
        for(int vertex: remainingVertices) queue.enq(vertex);
        SoftBarrier sBarrier = new SoftBarrier();
        
        int extraThreadsWanted = (int)Math.ceil(queue.size()/(double)parallelTrimThreshold) - 1;
        int extraThreadsGranted = processRequest(extraThreadsWanted);
        
        Thread[] threads = new Thread[extraThreadsGranted];
        for(int i = 0; i < threads.length; i++){
            SimpleTrimRunnable j = new SimpleTrimRunnable(queue, markedColor, sBarrier, true);
            threads[i] = new Thread(j);
        }
        SimpleTrimRunnable r = new SimpleTrimRunnable(queue, markedColor, sBarrier, false);
        if(DEBUG6) {
            if(extraThreadsGranted > 0) debugPrint(
                    "Starting "+ extraThreadsGranted + 
                    " worker SimpleTrimRunnables."
            );
        }
        for(Thread thread: threads){
            sBarrier.workerCheckIn();
            thread.start();
        }
        r.run();
    }
    
    /**
     * Does a single FW-BW iteration. Doesn't populate a BW set.
     * @param initialVertex: The starting vertex for this single FWBW iteration.
     * @return The size of the SCC found by this FWBW iteration.
     */
    Set<Integer> singleFwBwPass(int initialVertex){
        int fwdCol = Integer.MAX_VALUE - 1;
        int sccCol = markedColor;
        
        SplittableSinglyLinkedQueue<Integer> q = new SplittableSinglyLinkedQueue();
        q.enq(initialVertex);
        G.vertices[initialVertex].color = fwdCol;
        
        
        BFSFunctionInterface callbackObj = new SimpleFwBFSFunc(q, 0, fwdCol);
        
        SoftBarrier sBarrier = new SoftBarrier();
        BFSRunnable r = new BFSRunnable(callbackObj, q, sBarrier, false);
        
        r.run();
        
        Set<Integer> sccSet = null;
        try{
            sccSet = FWBW.setType.getClass().newInstance();
        } catch(Exception e){System.out.println(e);System.exit(0);}
        
        G.vertices[initialVertex].color = sccCol;
        sccSet.add(initialVertex);
        q.enq(initialVertex);
        remainingVertices.remove(initialVertex);
        
        BFSFunctionInterface callbackObj2 = new SimpleBwBFSFunc(q, sccSet, fwdCol, sccCol, remainingVertices);
        BFSRunnable r2 = new BFSRunnable(callbackObj2, q, sBarrier, false);
        
        r2.run();
        
        if(DEBUG7) {
            //if(sccSet.size() > 1)
                FWBW.debugPrint("SCC found by single FW-BW pass" +
                    " has size " + sccSet.size());
        }
        outputSCC(sccSet);
        return sccSet;
    }
    
    // Thanks wikipedia for pseudocode =)
    void tarjan(int currentElem, int srchSpaceCol, int sccCol){ 
        // auxiliary variables for cleaner code
        Vertex childVertex;
        TarjanData childData;
        TarjanData currentElemData;
        
        currentElemData = tarjanFields.get(currentElem);
        currentElemData.index = tarjanIndexCounter;
        currentElemData.lowLink = tarjanIndexCounter;
        
        tarjanIndexCounter++;
        stack.push(currentElem);
        currentElemData.onStack = true;
        
        for(int child: G.vertices[currentElem].outNeighbours){
            childVertex = G.vertices[child];
            if(childVertex.color != srchSpaceCol) continue; // ignore vertices already assigned to SCCs
            childData = tarjanFields.get(child);
            if(childData.index == 0){
                tarjan(child, srchSpaceCol, sccCol);
                currentElemData.lowLink = Math.min(currentElemData.lowLink, childData.lowLink);
            } else if(childData.onStack){
                currentElemData.lowLink = Math.min(currentElemData.lowLink, childData.index);
            }
        }
        
        if(currentElemData.lowLink == currentElemData.index){
            Set<Integer> sccSet = null;
            try{
                sccSet = setType.getClass().newInstance();
            } catch(Exception e){System.out.println(e);System.exit(0);}
            
            int currentBackTrackElem = stack.pop();
            tarjanFields.get(currentBackTrackElem).onStack = false;
            sccSet.add(currentBackTrackElem);
            G.vertices[currentBackTrackElem].color = sccCol;
            remainingVertices.remove(currentBackTrackElem);
            
            while(currentElem != currentBackTrackElem){
                currentBackTrackElem = stack.pop();
                tarjanFields.get(currentBackTrackElem).onStack = false;
                sccSet.add(currentBackTrackElem);
                G.vertices[currentBackTrackElem].color = sccCol;
                remainingVertices.remove(currentBackTrackElem);
            }
            if(DEBUG8) if(sccSet.size() > 1) debugPrint("Tarjan found SCC of size " + sccSet.size());
            outputSCC(sccSet);
        }
    }
    
    // This class holds the index and lowlink data fields necessary for Tarjan 
    // execution. The color field in Vertex cannot be used because it is needed
    // to exclude vertices that have already been removed by an earlier procedure
    // from the DFS.
    private class TarjanData{
        int index;
        int lowLink;
        boolean onStack;
        TarjanData(int index, int lowLink, boolean onStack){
            this.index = index;
            this.lowLink = lowLink;
            this.onStack = onStack;
        }
    }
}
