/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GraphStuff;
import DataStructures.ConcurrentHashSet;
import GraphStuff.Probability.ProbabilityBonusFunction;
import DataStructures.ConditionHardBarrier;
import java.io.File;
import java.io.PrintWriter;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author Benjamin
 */
public class RandomGraphGen {

    ProbabilityBonusFunction[] bonuses;
    RandomGraphGen(ProbabilityBonusFunction[] bonuses){
        this.bonuses = bonuses;
    }
    
    /**
     * p: A decimal number representing the edge probability
     * n: A integer representing the number of vertices
     * tgtFile: A string for the filename of the output file
     */
    public void generate(int n, double p, String tgtFile, int nThreads) throws Exception {
        // TODO: make these into command line arguments
        //int n = 1000;
        //double p = 4.0/n;
        File outFile = new File(tgtFile);
        PrintWriter writer;
        if(!outFile.isFile()){
            try {
                outFile.createNewFile();
            } catch(Exception e) {
                System.out.println("Couldn't create new file");
            }
            
        }
        //try{writer = new PrintWriter(outFile);} catch(Exception e){}
        writer = new PrintWriter(outFile);
        // create TreeSet for each vertex that will store and sort in neighbors.
        WrapSet[] inNeighbors = new WrapSet[n]; // WrapSet class created to facilitate array of generic objects.
        WrapSet[] outNeighbors = new WrapSet[n];
        for(int i = 0; i < n; i++){
            inNeighbors[i] = new WrapSet();
            outNeighbors[i] = new WrapSet();
        }
        
        // explain to human reader how the file works at the top of the file.
        printHelp(writer);
        writer.print("Number of vertices: " + n);
        
        // start parallelism  here
        AtomicInteger currentIdx = new AtomicInteger(0);
        ConditionHardBarrier barrier = new ConditionHardBarrier(nThreads);
        Thread[] threads = new Thread[nThreads -1];
        for(int i = 0; i < nThreads - 1; i++){
            GraphGenRunnable j = new GraphGenRunnable(outNeighbors, inNeighbors, p, bonuses, barrier, i, currentIdx);
            threads[i] = new Thread(j);
        }
        GraphGenRunnable r = new GraphGenRunnable(outNeighbors, inNeighbors, p, bonuses, barrier, nThreads - 1, currentIdx);
        for(Thread thread: threads){
            thread.start();
        }
        r.run();
        
        // actual file writing here
        for(int i = 0; i < n; i++){ // forall vertices
            writer.print("\n* " + i + " : " + outNeighbors[i].set.size() + " " +
                inNeighbors[i].set.size() + "\n");
            for(int O: outNeighbors[i].set){
                writer.print(O + " ");
            }
            writer.print("\n");
            for(int I: inNeighbors[i].set){
                writer.print(I + " ");
            }
        }
        
        writer.close();
    }
    
    static class WrapSet{
        ConcurrentHashSet<Integer> set;
        WrapSet(){
            this.set = new ConcurrentHashSet<>();
        }
    }
    
    static void printHelp(PrintWriter writer){
        writer.print("At the top of the file find the number of vertices. Each"
                + " entry starts with \"* [vertex#]: #outNeighbours "
                + "#inNeighbors\". On the 2 lines below the start of each entry"
                + " the out-neighbours and in-neighbours are listed in order\n");
    }
}
