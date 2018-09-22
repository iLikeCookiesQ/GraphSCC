/**
 * TODO: 
 * Move some assignments in FWBW class to constructor to allow execution of
 * both FW-BW and MultiStep in one run.
 * 
 * Rework TopLevel to take in command line arguments
 * 
 * Rework TrimRunnable into something that uses the ParallelizationSupport package.
 *  - lots of work.
 *      FWBW.trim() stays mostly the same.
 *      FWBW.trimAux now uses ParallelizationSupport package.
 *      Both the above methods still take a <T extends Trimmer> parameter.
 *      Trimmer is still an abstract class, but implements ElementProcessor
 *          instead of Runnable. It is probably best to move the void setup() 
 *          method into the abstract class. That saves copying that method into
 *          every ElementProcessor.
 *      Change ElementProcessor into an interface. Get rid of threadId. 
 * 
 * TrimRunnable needs a smaller rework regardless.
 * It needs the grabWhenever, or nPerGrab logic.
 * 
 * Perhaps move recounting logic to the start of the trimming class. Then the
 * dedicated recounter invocations wouldn't be needed in FWBW.
 * 
 * Could further optimize GraphGenRunnable to not synchronize accesses to
 * in-neighbor sets.
 * 
 * Enable parallelization for simple sweeps, and add high constants for them.
 * 
 * Create a Sweeper class that parallelizes a sweep over an Iterable.
 * Ideally with nPerGrab logic. The Sweeper also takes a callback object that
 * defines what should be done per element. Use this to parallelize
 * (Sweeper class done, it is called ParallelIterator)
 *      FWBW.recountNeighbors()     DONE
 *      FWBW.updateSccNeighbors()   DONE
 *      MultiStep.InitializeColors  DONE
 *      The Hong part where it collects the different WCCs after the WCC coloring
 *      step has finished.
 *
 * 
 * Below is done
 * 
 * Fix race condition in Hong TrimTwoRunnable that causes two threads to output
 * the same size 2 element. (It gets output once too much)
 * 
 * Parallelize MultiStep.getTopN()
 * 
 * Create an abstract class Trimmer extends Runnable. Then make TrimRunnable extend
 * this class. (TrimTwoRunnable and HongTrimRunnable extend it) Then rework 
 * FWBW.trimAux() to take a Trimmer.class as a parameter. Trimmer should have void setup() 
 * method that functions as an improvised constructor. It can be called from trimAux().
 * Then, the various trimming steps in Hong can use FWBW.trimAux() to manage the 
 * parallelization of the Trimmer s.
 * 
 * Rework TopLevel so algorithm selection is done with a switch instead of booleans
 * 
 * Rework TrimRunnable so that an extending class can override some functionality.
 *      - Hong should be able to do the post FWBW trimming steps without running 
 *        into trouble with the vertices of the last fwdSet from the last FWBW 
 *        iteration having a different color.  
 * 
 * Rework SimpleFWCallback and SimpleBWCallback to avoid vertices of a specific
 * color, instead of only add vertices of a specific color to the BFS queue.
 * This would let the parallel FW-BW part of Hong make use of them. ie. only vertices
 * that have the global SCCcol assigned are ignored. This way, vertices that were 
 * in the fwdSet of the previous iteration are not ignored.
 * 
 * Add logic to assign the topN vertices the highest color.
 *     - Must change initializeColors() to account for topN vertices.
 *     - Must take care when collecting roots.
 *         - Add special roots to the queue first.
 *     - Must assign correct srchSpaceCol to SimpleBWCallback in
 *       ColoringSecondPhaseRunnable when dealing with special roots.
 *         - Not their vertex identifier.
 *
 * 
 * Old ideas below
 * 
 * Alternative to the TrimRunnable rework above, just make NeighborRecounter use an
 * "avoidCol" instead of a srchSpaceCol, change how the colors work in FWBW. To
 * make that work. Globally set SccCol s and trimCol s to Integer.MIN_VALUE for
 * FWBW, MultiStep, and Hong. 
 */
package GraphStuff;
import GraphStuff.Probability.ProbabilityBonusFunction;
import GraphStuff.Probability.LinearRampBonus;
import GraphStuff.MultiStep.MultiStep;
import GraphStuff.FWBW.FWBW;
import DataStructures.Graph;
import DataStructures.PrintableScc;
import GraphStuff.Hong.Hong;
import GraphStuff.Probability.BucketBonus;
import GraphStuff.Probability.NestedBuckets;
import GraphStuff.Probability.StackingNestedBuckets;
import GraphStuff.Probability.NormallyDistributedBonus;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 * @author Benjamin
 */
public class TopLevel {
    //final static boolean AUTOMATICBUCKETING = false;
    // These flags affect outputting after runtime.
    final static boolean MAINOUTPUT = true; 
    // Prints SCC sizes in order of size. 
    final static boolean SORTEDOUTPUT = true;
    // Only takes effect when SORTEDOUTPUT is false. Prints each member of an SCC
    final static boolean PRINTSCCELEMENTS = false;
    
    public static void main(String[] args) throws Exception{
        final boolean GENERATEFILE = false;
        final boolean LOADGRAPH = true;
        // 1: FWBW, 2: MultiStep, 3: Hong, other: none
        final int algorithmSelectorSwitch = 1;
        
        
        // command line arguments
        final int N = 50000;
        final double P = 0.0/N; // Base chance for an edge to exist
        
        final ProbabilityBonusFunction bucket = new BucketBonus(N, 10.0, 20);
        final ProbabilityBonusFunction gaussian = new NormallyDistributedBonus(N, 0.5);
        
        ProbabilityBonusFunction nestedBuckets, stackingNestedBuckets;
        final double[] magnitudes = {1.5, 1.3, 1.1};
        int[] bucketDefinition = {100, 100, 100};
        nestedBuckets = new NestedBuckets(N, bucketDefinition, magnitudes);
        stackingNestedBuckets = new StackingNestedBuckets(N, bucketDefinition, magnitudes);
        //if(AUTOMATICBUCKETING) autoAssignBuckets(N, nestedBuckets, bucketDefinition, magnitudes);
        
        final ProbabilityBonusFunction linear = new LinearRampBonus(N, 0.05, 1);
        
        // Choose what bonuses to activate
        final ProbabilityBonusFunction[] bonuses = {
            //bucket
            //,nestedBuckets
            stackingNestedBuckets
            ,linear
        };
        
        //final String tgtFile = "PrettyGraph.txt";
        //final String tgtFile = "SmoothCurve.txt";
        //final String tgtFile = "BigSCC50kvert.txt";
        //final String tgtFile = "7scc50kvert.txt";
        final String tgtFile = "HongGraph.txt";
        //final String tgtFile = "graph.txt";
        final int nThreads = 8;
        final int fileGenerationThreads = 7;
        // end arguments
        
        //long timeStart, totalDuration;
        
        if(GENERATEFILE){            
            RandomGraphGen generator = new RandomGraphGen(bonuses);
            timeStart = System.currentTimeMillis();
            generator.generate(N, P, "graph.txt", fileGenerationThreads);     
            totalDuration = System.currentTimeMillis() - timeStart;
            System.out.println("The graph was generated in " + totalDuration + " milliseconds.");
        }
        
        //Graph G;
        if(LOADGRAPH){
            GraphLoader factory = new GraphLoader(tgtFile);
            G = factory.loadGraph();           
        }
        
        Set<Integer> all = FWBW.setType.getClass().newInstance();
        for(int i = 0; i < G.vertices.length; i++){
            all.add(i);
        }
        
        System.out.println("Graph has " + G.vertices.length + " vertices.");
        
        switch(algorithmSelectorSwitch){
            case 1: 
                performFwbw(nThreads, all);
                break;
            case 2:
                performMultiStep(nThreads, all);
                break;
            case 3:
                performHong(nThreads, all);
                break;
            default:
                System.out.println("No algorithm selected.");
                break;
        }
        
        if(FWBW.DEBUG6) FWBW.debugPrint(" Main checking out.");
        FWBW.checkOutThread();
        
        // Spin until other threads are done.
        while(!FWBW.allThreadsFinished.get()){}
        if(FWBW.DEBUG6) FWBW.debugPrint(" Main finished.");
        
        postReport(System.currentTimeMillis() - timeStart);
    }
    
    static void postReport(long totalDuration){
        System.out.println("Done.");
        if(MAINOUTPUT) outputSccs();
        
        int trivialCount;
        if(FWBW.TRIVIALCOUNTING){
            trivialCount = FWBW.nTrivialComponents.get();
        }
        else {
            trivialCount = FWBW.G.vertices.length - FWBW.combinedSccSize.get();
        }
        System.out.println("#non-trivial components: " + FWBW.nonTrivialSccCounter);
        System.out.println("#trivial components: " + trivialCount);
        System.out.println("Runtime: " + totalDuration + " milliseconds.");
        if(FWBW.THREADUTILDIAG){
            double threadUtilization = FWBW.threadUtilizationCounter /((double)(totalDuration*FWBW.threadPoolSize));
            System.out.println("Thread utilization: " + threadUtilization);
        }
    }
    
    static void outputSccs(){
        if(!SORTEDOUTPUT){
            for(PrintableScc scc: FWBW.getSccList()){
                System.out.println("SCC with size " + scc.getSize() + ":");
                if(!PRINTSCCELEMENTS) continue;
                for(int i: scc.getIterable()){
                    System.out.print(i + " ");
                }
                System.out.print("\n");
            }
        } else{
            HashMap<Integer, Integer> sizeFreq = new HashMap();
            int currentSize;
            for(PrintableScc scc: FWBW.getSccList()){
                currentSize = scc.getSize();
                if(!sizeFreq.containsKey(currentSize)){
                    sizeFreq.put(currentSize, 1);
                } else {
                    sizeFreq.put(currentSize, sizeFreq.get(currentSize) + 1);
                }
            }
            
            TreeSet<Integer> sorted = new TreeSet(sizeFreq.keySet());
            for(int sccSize: sorted){
                int sizeOccurrence = sizeFreq.get(sccSize);
                for(int i = 0; i < sizeOccurrence; i++){
                    System.out.println("SCC with size " + sccSize + ":");
                }
            }
        }
    }
    
    static Graph G;
    static long timeStart, totalDuration;
    
    static void performFwbw(int nThreads, Set<Integer> all) throws Exception {
        FWBW fwbw = new FWBW(G, nThreads);
        System.out.print("Running FW-BW...\n");
        
        timeStart = System.currentTimeMillis();
        if(FWBW.THREADUTILDIAG) FWBW.lastChangeTimeStamp = timeStart;
        
        fwbw.runFWBW(all, 0);   
    }
    
    static void performMultiStep(int nThreads, Set<Integer> all) throws Exception {
        MultiStep multiStep = new MultiStep(G, nThreads, all);
        System.out.print("Running MultiStep...\n");
        
        timeStart = System.currentTimeMillis();
        if(FWBW.THREADUTILDIAG) FWBW.lastChangeTimeStamp = timeStart;
        
        multiStep.runMultiStep();   
    }
    
    static void performHong(int nThreads, Set<Integer> all) throws Exception {
        Hong hong = new Hong(G, nThreads, all);
        System.out.print("Running Hong...\n");
        
        timeStart = System.currentTimeMillis();
        if(FWBW.THREADUTILDIAG) FWBW.lastChangeTimeStamp = timeStart;
        
        hong.runHong();
    }
    
    
    // creates buckets by taking roots of graph size
    static void autoAssignBuckets(
            int N,
            ProbabilityBonusFunction nestedBuckets,
            int[] bucketDefinition,
            double[] magnitudes
    ){
        final int bucketDepth = bucketDefinition.length;
        int root = (int)Math.pow(N, 1/(double)(bucketDepth + 1));
        bucketDefinition = new int[bucketDepth];
        for(int i = 0; i < bucketDepth; i++){
            bucketDefinition[i] = root;
        }
        nestedBuckets = new NestedBuckets(N, bucketDefinition, magnitudes);
    }
}
