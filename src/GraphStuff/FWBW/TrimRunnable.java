/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GraphStuff.FWBW;

import DataStructures.ConditionHardBarrier;
import DataStructures.SoftBarrier;
import DataStructures.Vertex;
import DataStructures.Graph;
import DataStructures.Tagging.Trimmer;
import java.util.LinkedList;
import java.util.Set;

/**
 * Legacy class. No longer in use.
 * 
 * This class is intended to be used for iterative trimming.
 * It updates neighbor counts and populates nextIterationSet for future
 * iterations.
 * @author Benjamin
 */
public class TrimRunnable //extends Trimmer 
{
    public Graph G;         // Graph pointer
    public Set<Integer> searchSpace; // Set pointer to unassigned vertices
    
    // We fill this with the neighbors of trimmed vertices.
    Set<Integer> nextIterationSet;   
    
    Set<Integer> trimSpace; // Vertices being checked for trimmability.
    public int srchSpaceCol, trimCol;
    ConditionHardBarrier hBarrier;
    SoftBarrier sBarrier;
    int threadOffset; // used for dividing data elements across threads
    boolean isWorkerThread;
    
    public LinkedList<Integer> trimmed = new LinkedList<>();
    
    public TrimRunnable(){}
    public TrimRunnable(
            Graph G, 
            Set<Integer> searchSpace,
            Set<Integer> trimSpace, 
            Set<Integer> nextIterationSet,
            int srchSpaceCol, int trimCol,
            ConditionHardBarrier hBarrier,
            SoftBarrier sBarrier,
            int threadOffset, boolean isWorker
    ){
        setup(G, searchSpace, trimSpace, nextIterationSet,
                srchSpaceCol, trimCol,
                hBarrier, sBarrier,
                threadOffset, isWorker);
        /*
        this.G = G;
        this.searchSpace = searchSpace;
        this.trimSpace = trimSpace;
        this.nextIterationSet = nextIterationSet;
        this.srchSpaceCol = srchSpaceCol;
        this.trimCol = trimCol;
        this.hBarrier = hBarrier;
        this.sBarrier = sBarrier;
        this.threadOffset = threadOffset;
        this.isWorkerThread = isWorker;//*/
    }
    
    //@Override // Pseudo contructor: useful if constructor can't be called.
    public final void setup(
            Graph G, 
            Set<Integer> searchSpace,
            Set<Integer> trimSpace, 
            Set<Integer> nextIterationSet,
            int srchSpaceCol, int trimCol,
            ConditionHardBarrier hBarrier,
            SoftBarrier sBarrier,
            int threadOffset, boolean isWorker
    ){
        this.G = G;
        this.searchSpace = searchSpace;
        this.trimSpace = trimSpace;
        this.nextIterationSet = nextIterationSet;
        this.srchSpaceCol = srchSpaceCol;
        this.trimCol = trimCol;
        this.hBarrier = hBarrier;
        this.sBarrier = sBarrier;
        this.threadOffset = threadOffset;
        this.isWorkerThread = isWorker;
    }
    
    public Vertex auxVertex; // helps for simpler code. 
    //public LinkedList<Integer> trimmed;
    //@Override
    public void run(){       
        //trimmed = new LinkedList(); // put all trimmed vertices here
        
        int child, parent;  // more auxiliaries
        int currentIdx = 0; // count data elements and distribute them across threads
        int threadCount = hBarrier.getThreadCount();
        for(int i: trimSpace){ // detect and trim trimmable vertices
            // ignore data elements not in this threads equivalence class           
            currentIdx++;
            if(currentIdx%threadCount != threadOffset) continue; 
            auxVertex = G.vertices[i];
            processElem(i);
        }
        
        // wait at the barrier: required so that no vertices get added to nextIterationSet and are then later trimmed
        // in the current iteration.
        
        hBarrier.checkIn();   
        
        for(int i: trimmed){ // add in and out neighbors of trimmed vertices to the nextIterationSet
            auxVertex = G.vertices[i];
            for(int j = 0; j < auxVertex.outNeighbours.length; j++){ // out neighbors
                child = auxVertex.outNeighbours[j];
                if(checkColors(G.vertices[child].color)){
                    //G.vertices[child].nIn--;
                    nextIterationSet.add(child);
                }
            }
            for(int j = 0; j < auxVertex.inNeighbours.length; j++){ // in neighbors
                parent = auxVertex.inNeighbours[j];
                if(checkColors(G.vertices[parent].color)){
                    //G.vertices[parent].nOut--;
                    nextIterationSet.add(parent);
                }
            }    
        }
        
        // wait at the barrier again. Required to complete trimming step on all threads
        // so that no more trimming happens while FW step is executed.
        if(!isWorkerThread){    // main thread
            try{
                sBarrier.mainCheckOut();
            } catch(Exception e){
                System.out.println(e);
                System.exit(0);
            }     
        } else{                 // worker thread
            sBarrier.workerCheckOut(); 
            //FWBW.threadsInUse.getAndDecrement();
            FWBW.checkOutThread();
        }
    }
    
    public boolean checkColors(int color){
        return color == srchSpaceCol;
    }
    
    /**
     * Evaluate whether the current vertex under investigation must be trimmed.
     * This is in method form so that the TrimTwo class can extend and override it.
     * @param i: the integer identifier of the current vertex under investigation.
     * @pre: auxVertex is the Vertex object of vertexId i
     */
    
    public void processElem(int i){
        auxVertex.nOut.set(countNeighbors(auxVertex.outNeighbours));
        
        if(auxVertex.nOut.get() == 0){
            trimThisOne(i);
            return;
        }
        
        auxVertex.nIn.set(countNeighbors(auxVertex.inNeighbours));
        
        if(auxVertex.nIn.get() == 0) trimThisOne(i);
        
        /* Old code
        if(auxVertex.nOut == 0 || auxVertex.nIn == 0){
            auxVertex.color = trimCol;
            searchSpace.remove(i);
            trimmed.add(i);
            if(FWBW.DEBUG2) {
                System.out.println("just trimmed vertex " + i + " searchSpace size: " + searchSpace.size());
            }
            if(FWBW.TRIVIALCOUNTING)FWBW.nTrivialComponents.getAndIncrement(); // quasi output this SCC
        }//*/
    }
    
    int neighborCounter;
    public int countNeighbors(int[] neighborArray){
        neighborCounter = 0;
        for(int neighbor: neighborArray){
            if(checkColors(G.vertices[neighbor].color)) neighborCounter++;
        }
        return neighborCounter;
    }
    
    private void trimThisOne(int vertexId){
        auxVertex.color = trimCol;
        searchSpace.remove(vertexId);
        trimmed.add(vertexId);
        if(FWBW.DEBUG2) {
            System.out.println("just trimmed vertex " + vertexId + " searchSpace size: " + searchSpace.size());
        }
        if(FWBW.TRIVIALCOUNTING)FWBW.nTrivialComponents.getAndIncrement(); // quasi output this SCC
    }
}
