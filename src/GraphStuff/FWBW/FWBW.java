/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GraphStuff.FWBW;

import GraphStuff.NeighborRecounterProcessor;
import DataStructures.ConditionHardBarrier;
import DataStructures.SoftBarrier;
import DataStructures.Vertex;
import DataStructures.Graph;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.ConcurrentSkipListSet;
import DataStructures.ConcurrentHashSet;
import DataStructures.ParallelizationSupport.ElementProcessor;
import DataStructures.ParallelizationSupport.ParallelIterator;
import DataStructures.SplittableSinglyLinkedQueue;
import GraphStuff.BFSFunctionInterface;
import GraphStuff.BFSRunnable;
import java.util.Set;
import DataStructures.PrintableScc;
import DataStructures.Tagging.Trimmer;
import DataStructures.UnboundedStack;
import GraphStuff.IterativeTrimProcessor;
import GraphStuff.MultiStep.MultiStep;
import GraphStuff.NeighborDecrementorProcessor;
import GraphStuff.NextIterationSetPopulatorProcessor;
import GraphStuff.SetFillerProcessor;
import java.lang.reflect.Constructor;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 *
 * @author Benjamin
 */
public class FWBW {
    public final static int parallelTrimThreshold = 10000; // minimum # vertices to spawn new thread in trimming step.
    //public final static int parallelSweepThreshold = 20000; // minimum # of vertices to spawn a new thread for low load sweeps over a set. (not in use)
    public final static int parallelRecountThreshold = 20000; // minimum # of vertices to spawn a new neighbor recounting thread.
    public final static int parallelNeighborDecrementingThreshold = 20000;
    public final static int parallelNextIterationSetPopulationThreshold = 10000;
    public final static int BFSQueueSplittingThreshold = 100; // minimum queue size before splitting a BFS queue and starting new thread.
    public final static boolean PARALLELIZEBFS = true;
    public final static boolean TRIM = true; // turns trimming on or off in both FWBW and MultiStep.
    /**
     * Chooses what method of trivial component counting is applied.
     * True does an increment for each time a size 1 scc is reported,
     * false adds the size of each non trivial scc together, and subtracts it
     * from the total number of vertices. False should be more efficient. 
     * True however, lets you spot bugs more easily.
     */
    final public static boolean TRIVIALCOUNTING = false; 
    final static boolean PRINTSCCSIZE = false; // print the size of detected non-trivial SCCs at runtime
    final static boolean PRINTSCCCONTENT = false; // print each element in non-trivial SCCs at runtime
    public final static boolean THREADUTILDIAG = true; // enables thread utilization diagnostic
    public final static Set<Integer> setType = new ConcurrentHashSet(); // decides the set type that will be used wherever sets are used. (many places)
    
    public final static boolean DEBUG = false;
    public final static boolean DEBUG1 = true; // displays number of trimmed vertices each invocation of the trim() method
    public final static boolean DEBUG2 = false;
    public final static boolean DEBUG3 = false;
    public final static boolean DEBUG4 = false; // displays number of trimmed vertices each trim iteration
    public final static boolean DEBUG5 = false; // prints whenever a thread is started or ends
    public final static boolean DEBUG6 = false; // prints the type of thread that is started
    
    public static Graph G;
    public final static AtomicBoolean allThreadsFinished = new AtomicBoolean(false);
    public final static AtomicInteger colorCounter = new AtomicInteger(1);
    // The next 2 data fields are used to track thread utilization.
    // The counter will be incremented by the total time threads have been
    // running since the last timestamp.
    // The timestamp tracks the last moment that threadsInUse changed.
    public static long threadUtilizationCounter = 0;
    public static long lastChangeTimeStamp;
    final static AtomicInteger threadsInUse = new AtomicInteger(1);
    public final static AtomicInteger nTrivialComponents = new AtomicInteger(0);
    public final static AtomicInteger nonTrivialSccCounter = new AtomicInteger(0);
    public final static AtomicInteger combinedSccSize = new AtomicInteger(0);
    public static int threadPoolSize;
    private static LinkedList<PrintableScc> sccList; // Store detected SCCs here
    
    public FWBW(Graph graph, int nThreads){
        G = graph;      
        threadPoolSize = nThreads;
        
        sccList = new LinkedList();
    }
    public FWBW(){
        // empty constructor takes the super calls from FWBWRunnable
    }
    
    public void runFWBW(Set<Integer> searchSpace, int srchSpaceCol)
        throws Exception
    {
        if(searchSpace.isEmpty()) return;
        // trimming
        if(TRIM){
            int trimCol = colorCounter.getAndIncrement();
            trim(IterativeTrimProcessor.class, searchSpace, srchSpaceCol, trimCol, null, false, true);
            
            if(DEBUG4) debugPrint("trim completed " + searchSpace.size() + " vertices remaining\n");
            if(searchSpace.isEmpty()) {
                if(DEBUG4) debugPrint("Trimmed entire searchSpace");
                return;
            }
        }
        
        // used later to determine best neighbor count updating method.
        int oldSize = searchSpace.size();
        
        // select first elem in searchSpace as our pivot
        int initialVertex = 0;
        for(int i: searchSpace){
            initialVertex = i;
            break;
        }
        int fwdCol = colorCounter.getAndIncrement();
        int sccCol = colorCounter.getAndIncrement();
        int bwdCol = colorCounter.getAndIncrement();
        
        Set<Integer> fwdSet = FW(searchSpace, srchSpaceCol, initialVertex, fwdCol);        
        Set<Integer> sccSet = setType.getClass().newInstance();        
        Set<Integer> bwdSet = BW(searchSpace, srchSpaceCol, sccSet, fwdSet, initialVertex, sccCol, fwdCol, bwdCol);
        
        outputSCC(sccSet);
        
        
        // Depending on the size of the SCC versus the size of the rest,
        // Either update the neighbour counts of vertices by iterating over the
        // SCC, or by fully recounting the amount of neighbors for the non SCC
        // vertices. Recounting the neighborcounts is necessary for the trimming
        // steps in recursive calls to remove any vertices.
        if(sccSet.size() > (0.5*oldSize)){
        //if(sccSet.size() > (searchSpace.size() + fwdSet.size() + bwdSet.size())){
            //debugPrint("Large SCC");
            recountNeighbours(fwdSet, fwdCol, true);
            recountNeighbours(bwdSet, bwdCol, true);
            
            //Recounting neighbors of the remainder isn't necessary, as by definition
            //no remainder vertices are adjacent to the SCC.
            
            //recountNeighbours(searchSpace, srchSpaceCol);
        } else {
            //updateSccNeighbors(sccSet, fwdCol, bwdCol);
            updateSccNeighbors(sccSet, sccSet.size());
        }//*/
        
        
        
        // recursive calls here on fwdSet, bwdSet, and searchSpace (the remainder)
        // make a new thread or run in current thread depending on available threads
        
        handleRecursion(fwdSet, bwdSet, searchSpace, fwdCol, bwdCol, srchSpaceCol);
        
        /*// Old code here. This is now included in the handleRecursion method.
        int extraThreadsGranted = processRequest(2);
        
        if(extraThreadsGranted == 0){
            new FWBWRunnable(fwdSet, fwdCol, false).run();
            new FWBWRunnable(bwdSet, bwdCol, false).run();
        } else if (extraThreadsGranted == 1){
            Thread thread = new Thread(new FWBWRunnable(fwdSet, fwdCol, true));
            if(DEBUG6) debugPrint("Starting " + 1 + " worker FWBWRunnable.");
            thread.start();
            new FWBWRunnable(bwdSet, bwdCol, false).run();
        } else if (extraThreadsGranted == 2){
            Thread thread = new Thread(new FWBWRunnable(fwdSet, fwdCol, true));
            Thread thread2 = new Thread(new FWBWRunnable(bwdSet, bwdCol, true));
            if(DEBUG6) debugPrint("Starting " + 2 + " worker FWBWRunnables.");
            thread.start();
            thread2.start();
        }
        new FWBWRunnable(searchSpace, srchSpaceCol, false).run();//*/
    }
    
    /**
     * Handles the recursive calls of the FW-BW algorithm.
     * Checks how many threads are available and creates a Runnable for each task.
     * Each Runnable is assigned to a Thread if available.
     * 
     * @param fwdSet: Forwards minus backwards closure from the pivot.
     * @param bwdSet: Backwards minus forwards closure from the pivot.
     * @param remainderSet: The rest of the graph that is in neither the forwards
     *                      or backwards closure.
     * @param fwdCol: Color of vertices in fwdSet.
     * @param bwdCol: Color of vertices in bwdSet.
     * @param remainderCol: Color of vertices in remainderSet.
     */
    public void handleRecursion(
            Set<Integer> fwdSet,
            Set<Integer> bwdSet,
            Set<Integer> remainderSet,
            int fwdCol,
            int bwdCol,
            int remainderCol
    ){
        int extraThreadsGranted = processRequest(2);
        
        if(extraThreadsGranted == 0){
            new FWBWRunnable(fwdSet, fwdCol, false).run();
            new FWBWRunnable(bwdSet, bwdCol, false).run();
        } else if (extraThreadsGranted == 1){
            Thread thread = new Thread(new FWBWRunnable(fwdSet, fwdCol, true));
            if(DEBUG6) debugPrint("Starting " + 1 + " worker FWBWRunnable.");
            thread.start();
            new FWBWRunnable(bwdSet, bwdCol, false).run();
        } else if (extraThreadsGranted == 2){
            Thread thread = new Thread(new FWBWRunnable(fwdSet, fwdCol, true));
            Thread thread2 = new Thread(new FWBWRunnable(bwdSet, bwdCol, true));
            if(DEBUG6) debugPrint("Starting " + 2 + " worker FWBWRunnables.");
            thread.start();
            thread2.start();
        }
        new FWBWRunnable(remainderSet, remainderCol, false).run();
    }
    
    int oldSrchSpaceSize;
    public <T extends Trimmer> void trim(
            Class<T> klass,
            Set<Integer> searchSpace,
            int srchSpaceCol,
            int trimCol,
            Set<Integer> firstIterationSubSet, // Optionally supply an existing set as argument.
            boolean countOnTheFly,
            boolean seek
    )
            throws Exception
    {
        if(DEBUG1) oldSrchSpaceSize = searchSpace.size();
        // the next section checks all vertices in trimSpace if they have 0 in
        // or out neighbors. If so, trim them, and add them to the trimmed set. 
        // Then forall elements in trimmed add the neighbors to nextIterationSet. 
        // Then repeat but swap the pointers of trimSpace and nextIterationSet.
        
        Set<Integer> nextIterationSet = setType.getClass().newInstance();
        Set<Integer> trimSpace;
        
        if(firstIterationSubSet == null) {
            trimSpace  = setType.getClass().newInstance();
            for(int i: searchSpace) trimSpace.add(i);
        }
        else{
            trimSpace = firstIterationSubSet;
        }
       
        /*// The following is a parallelized way to populate trimSpace with the vertices of searchSpace
        // It might not be worth paralellizing though, leaving it turned off for now.
        // To turn it on, uncomment it and then comment the for(int i: searchSpace) loop below it.
        int extraThreadsGranted = decideNThreads(searchSpace, parallelSweepThreshold);
        ElementProcessor<Integer> callbackObj = new SetFillerProcessor(trimSpace);
        ParallelIterator<Integer> pi = new ParallelIterator(searchSpace, searchSpace.size(), extraThreadsGranted + 1, callbackObj);
        pi.iterate();
        for(int i = 0; i < extraThreadsGranted; i++) checkOutThread(); //*/
        
        
        
        while(!trimSpace.isEmpty()){
            //if(DEBUG4) debugPrint("trimSpace before iteration: " + trimSpace.size());
            trimAux(klass, searchSpace, trimSpace, nextIterationSet, srchSpaceCol, trimCol, countOnTheFly, seek);
            trimSpace = nextIterationSet;
            nextIterationSet = setType.getClass().newInstance();
            if(DEBUG2 || DEBUG3) debugPrint("finished trim iteration, next set size: " + trimSpace.size());
        }
        if(DEBUG1) debugPrint("Trimmed " + (oldSrchSpaceSize - searchSpace.size()) 
                + " in this trimming phase.");
    }
    
   
    /**
     * Auxiliary function to support parallel iterative trimming
     * @param ep: A callback object of the trimming functionality that we want to use.
     * (Other algorithms have different trimming nuances and use different ElementProcessors
     * while calling this method.)
     * @param searchSpace: The Set object of the remaining vertices in the graph.
     * linked here so that trimmed vertices may be removed from this set.
     * @param trimSpace: The set of vertices being evaluated for trimming.
     * @param nextIterationSet: Next trimming iteration will be over this set.
     * @param srchSpaceCol: Allows the method to see what vertices are unassigned
     * without making an expensive set.contains() call.
     * @param trimCol: Assign this color to the color field of trimmed vertices.
     * @throws Exception 
     */
    public <T extends Trimmer> void trimAux(
            Class<T> klass,
            Set<Integer> searchSpace,
            Set<Integer> trimSpace,
            Set<Integer> nextIterationSet,
            int srchSpaceCol,
            int trimCol,
            boolean countOnTheFly,
            boolean seek
    ) throws Exception {
        // First run ParallelIterator with ep as its callback.
        SoftBarrier sBarrier = new SoftBarrier();
        if(DEBUG3) System.out.println(trimSpace.size());
        
        int extraThreadsGranted = decideNThreads(trimSpace.size(), parallelTrimThreshold);
        
        SplittableSinglyLinkedQueue<Integer>[] trimmedSets = new SplittableSinglyLinkedQueue[extraThreadsGranted + 1];
        for(int i = 0; i < extraThreadsGranted + 1; i++) trimmedSets[i] = new SplittableSinglyLinkedQueue();
        Trimmer trimmer = klass.newInstance();
        trimmer.setup(G, searchSpace, trimmedSets, srchSpaceCol, trimCol, sBarrier, countOnTheFly, seek);
        
        ParallelIterator<Integer> pi = new ParallelIterator(trimSpace, trimSpace.size(), extraThreadsGranted + 1, trimmer);
        if(DEBUG6){
            if(extraThreadsGranted > 0){
                debugPrint("Starting " + extraThreadsGranted + " worker IterativeTrimProcessor threads.");
            }
        }
        pi.iterate();
        for(int i = 0; i < extraThreadsGranted; i++) checkOutThread();
        
        SplittableSinglyLinkedQueue<Integer> allTrimmed = mergeAllQueues(trimmedSets);
        
        
        /*/////////////////////// non parallelized nextIterationSet population.
        updateSccNeighbors(allTrimmed, allTrimmed.size());
        
        for(int elem: allTrimmed){
            for(int parent: G.vertices[elem].inNeighbours){
                if(seek){
                    if(G.vertices[parent].color == srchSpaceCol) nextIterationSet.add(parent);
                } else{
                    if(G.vertices[parent].color != srchSpaceCol) nextIterationSet.add(parent);
                }
            }
            for(int child: G.vertices[elem].outNeighbours){
                if(seek){
                    if(G.vertices[child].color == srchSpaceCol) nextIterationSet.add(child);
                } else{
                    if(G.vertices[child].color != srchSpaceCol) nextIterationSet.add(child);
                }
            }
        }///////////////////////////////*/// end non parallelized nextIterationSet population.
        
        ///////////////////////////////////// parallelized nextIterationSet population.
        int extraThreadsGranted2 = decideNThreads(allTrimmed.size(), parallelNextIterationSetPopulationThreshold);
        ElementProcessor<Integer> callbackObj2 = new NextIterationSetPopulatorProcessor(G, srchSpaceCol, nextIterationSet, seek);
        ParallelIterator<Integer> pi2 = new ParallelIterator(allTrimmed, allTrimmed.size(), extraThreadsGranted2 + 1, callbackObj2);
        if(DEBUG6){
            if(extraThreadsGranted2 > 0){
                debugPrint("Starting " + extraThreadsGranted2 + " worker NextIterationSetPopulatorProcessor threads.");
            }
        }
        pi2.iterate();
        for(int i = 0; i < extraThreadsGranted2; i++) checkOutThread();
        ///////////////////////////////*/// end parallelized nextIterationSet population.
    }
    
    public SplittableSinglyLinkedQueue<Integer> mergeAllQueues(SplittableSinglyLinkedQueue<Integer>[] queues){
        SplittableSinglyLinkedQueue<Integer> returnValue = new SplittableSinglyLinkedQueue();
        for(int i = 0; i < queues.length; i++){
            if(MultiStep.DEBUG8) debugPrint("appending q of size " + queues[i].size());
            returnValue.append(queues[i]);
        }
        return returnValue;
    }
    
    // Starting at vertex initialVertex, it does BFS and marks all reached 
    // vertices with color fwdCol. Finally the reached vertices are also returned
    // in a ConcurrentSkipListSet.
    private Set<Integer> FW
        (
            Set<Integer> searchSpace,
            int srchSpaceCol,
            int initialVertex,
            int fwdCol
        ) 
            throws InstantiationException, IllegalAccessException
    {
        Set<Integer> fwdSet = setType.getClass().newInstance();
        
        SplittableSinglyLinkedQueue<Integer> q = new SplittableSinglyLinkedQueue();
        //Queue<Integer> q = new LinkedList();
        
        q.enq(initialVertex);
        fwdSet.add(initialVertex);
        searchSpace.remove(initialVertex);
        G.vertices[initialVertex].color = fwdCol;
        
        if(DEBUG) System.out.print("\n" + initialVertex + "\n");
        
        SoftBarrier sBarrier = new SoftBarrier();
        BFSFunctionInterface callbackObject = new FwBFSFunc(searchSpace, fwdSet, srchSpaceCol, fwdCol, q);
        BFSRunnable r = new BFSRunnable(callbackObject, q, sBarrier, false);
        r.run();
        
        return fwdSet;
    }
    
    // does the backwards search, adding everything that was also in the forwards search to the sccSet,
    // and everything that wasn't to a new bwdSet, and returns bwdSet.
    private Set<Integer> BW
        (
            Set<Integer> searchSpace,
            int srchSpaceCol, 
            Set<Integer> sccSet,
            Set<Integer> fwdSet,
            int initialVertex, int sccCol, int fwdCol, int bwdCol
        )
            throws InstantiationException, IllegalAccessException
    {
        Set<Integer> bwdSet = setType.getClass().newInstance();
        SplittableSinglyLinkedQueue<Integer> q = new SplittableSinglyLinkedQueue();
        
        q.enq(initialVertex);
        sccSet.add(initialVertex);
        fwdSet.remove(initialVertex);
        G.vertices[initialVertex].color = sccCol;
        
        SoftBarrier sBarrier = new SoftBarrier();
        BFSFunctionInterface callbackObject = new BwBFSFunc(searchSpace, fwdSet, bwdSet, sccSet, 
            srchSpaceCol, fwdCol, bwdCol, sccCol, q);
        BFSRunnable r = new BFSRunnable(callbackObject, q, sBarrier, false);
        r.run();
        
        return bwdSet;
    }
    
    // The following 4 methods deal with printing the contents of an SCC
    // to the console.
    final static Object printLock = new Object();
    public static void outputSCC(Set<Integer> set){
        int size = set.size();
        outputSCCAux(set, size);
    }
    
    public static void outputSCC(SplittableSinglyLinkedQueue<Integer> set){
        int size = set.size();
        outputSCCAux(set, size);
    }
    
    public static void outputSCC(UnboundedStack<Integer> set){
        int size = set.size();
        outputSCCAux(set, size);
    }
    
    private static void outputSCCAux(Iterable<Integer> set, int size){
        if(size == 1){
            if(TRIVIALCOUNTING) nTrivialComponents.getAndIncrement();
            return;
        } 
        if(!TRIVIALCOUNTING) combinedSccSize.getAndAdd(size);
        
        nonTrivialSccCounter.getAndIncrement();
        
        synchronized(sccList) {
            sccList.add(new PrintableScc(size, set));
        }
        
        if(!PRINTSCCSIZE) return;
        synchronized(printLock){
            System.out.println("==============SCC with size " + size + ":");
            if(!PRINTSCCCONTENT) return;
            for(int i: set){
                System.out.print(i + " ");
            }
            System.out.print("\n");
        }
    }
    
    // Returns the sccList.
    public static LinkedList<PrintableScc> getSccList(){
        return sccList;
    }
    
    /**
     * Stuff below is no longer in use by FWBW as TrimRunnable recounts neighbor
     * counts in the class itself. MultiStep still uses these functions to get
     * accurate neighborcounts for the topN functionality to work.
     */ 
     
    /**
     * To facilitate the next trimming step, we have to update the neighborcounts.
     * We can either do this by recounting the neighbors of all remaining vertices,
     * or by decrementing the neighbor count of the vertices in a found SCC.
     * 
     * The following 2 methods do those things. Whichever is faster depends on
     * the size of the SCC compared to the size of the rest of the remaiming graph.
    */
    
    // recounts neighbors by explicitly counting neighbors that have not been removed
    public void recountNeighbours(Set<Integer> set, int setCol, boolean seek){ 
        int extraThreadsGranted = decideNThreads(set.size(), parallelRecountThreshold);
        ElementProcessor<Integer> callbackObj = new NeighborRecounterProcessor(setCol, G, seek);
        ParallelIterator<Integer> pi = new ParallelIterator(set, set.size(), extraThreadsGranted + 1, callbackObj);
        if(DEBUG6){
            if(extraThreadsGranted > 0){
                debugPrint("Starting " + extraThreadsGranted + " worker NeighborRecounter threads.");
            }
        }
        pi.iterate();
        for(int i = 0; i < extraThreadsGranted; i++) checkOutThread();
    }
    
    // Updates neighbors of vertices in a found SCC by decrementing their in or out degrees
    public void updateSccNeighbors(
            Iterable<Integer> iterable,
            int size
    ){
        int extraThreadsGranted = decideNThreads(size, parallelNeighborDecrementingThreshold);
        ElementProcessor<Integer> callbackObj = new NeighborDecrementorProcessor(G);
        ParallelIterator<Integer> pi = new ParallelIterator(iterable, size, extraThreadsGranted + 1, callbackObj);
        if(DEBUG6){
            if(extraThreadsGranted > 0){
                debugPrint("Starting " + extraThreadsGranted + " worker NeighborDecrementor threads.");
            }
        }
        pi.iterate();
        for(int i = 0; i < extraThreadsGranted; i++) checkOutThread();
    }
    
    /**
     * Decides how many new threads we should make based on the size of the set
     * being iterated over, and the minimum size for making an extra thread.
     * Calling this function immediately reserves that number of threads for use
     * until checkOutThread() is called to free up thread slots.
     * 
     * @param size: The size collection being iterated over.
     * @param minimumParallelizationSize: The minimum size of set at which we 
     * make 1 extra thread.
     * @return The number of extra threads that is recommended to make.
     */
    public int decideNThreads(int size, int minimumParallelizationSize){
        if(size == 0) return 0;
        int extraThreadsWanted = (int)Math.ceil(size/(double)minimumParallelizationSize) - 1;
        int extraThreadsGranted = processRequest(extraThreadsWanted);
        return extraThreadsGranted;
    }
    
    // handles requests for extra threads and makes sure we never make more
    // threads than threadPoolSize. 
    /*
     * k: the number of threads you want to request
     * returns the number of granted threads up to k.
     */
    
    public static int processRequest(int k){
        if(k == 0) return 0; // dont acquire lock
        synchronized(threadsInUse){
            int inUse = threadsInUse.get();    
            if(inUse == threadPoolSize) return 0;
            int available = threadPoolSize - inUse;
            if(available >= k){
                threadsInUse.set(inUse + k);
                if(DEBUG5){
                    synchronized(printLock){
                        System.out.println("Created " + k 
                                + " new threads, threadsInUse: " + (inUse + k));
                    } 
                }
                if(THREADUTILDIAG) updateThreadUtilCounter(inUse);
                return k;
            } else {
                threadsInUse.set(threadPoolSize);
                if(DEBUG5){
                    synchronized(printLock){
                        System.out.println("Created " + available 
                                + " new threads, threadsInUse: " + threadPoolSize);
                    } 
                }
                if(THREADUTILDIAG) updateThreadUtilCounter(inUse);
                return available;
            }
        }
    }
    
    // call to denote that a thread is finished so that it frees up
    // a thread in the thread pool to use by other methods or threads.
    public static void checkOutThread(){
        synchronized(threadsInUse){
            int k = threadsInUse.decrementAndGet();
            if(DEBUG5){
                synchronized(printLock){
                    System.out.println("Thread closed, threadsInUse: " + k);
                }
            }
            if(THREADUTILDIAG) updateThreadUtilCounter(k + 1);
            if(k == 0){
                allThreadsFinished.set(true);
            }
        }
    }
    
    // This method increases the threadUtilizationCounter by a millisecond
    // for every millisecond*(threads currently active).
    // It is only ever called inside the threadsInUse lock
    private static void updateThreadUtilCounter(int nThreads){
        long timeStamp = System.currentTimeMillis();
        threadUtilizationCounter += (nThreads)*(timeStamp - lastChangeTimeStamp);
        lastChangeTimeStamp = timeStamp;
    }
    
    public static void debugPrint(String s){
        synchronized(printLock){
            System.out.println(s);
        }
    }
}

