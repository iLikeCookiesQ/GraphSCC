/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GraphStuff;

import DataStructures.ConditionHardBarrier;
import DataStructures.Graph;
import DataStructures.ParallelizationSupport.ElementProcessor;
import DataStructures.SoftBarrier;
import DataStructures.SplittableSinglyLinkedQueue;
import DataStructures.Tagging.Trimmer;
import DataStructures.Vertex;
import GraphStuff.FWBW.FWBW;
import java.util.LinkedList;
import java.util.Set;

/**
 *
 * @author Benjamin
 */
public class IterativeTrimProcessor extends Trimmer {

    public Graph G;         // Graph pointer
    public Set<Integer> searchSpace; // Set pointer to unassigned vertices
    public SplittableSinglyLinkedQueue<Integer>[] trimmedSets; // Store trimmed vertices here.
    public SplittableSinglyLinkedQueue<Integer> trimmedSet;
    public int srchSpaceCol;
    public int trimCol;
    public SoftBarrier sBarrier;
    public boolean countOnTheFly;
    public boolean seek;
    
    public IterativeTrimProcessor(){}
    
    public IterativeTrimProcessor(
            Graph G, 
            Set<Integer> searchSpace, 
            SplittableSinglyLinkedQueue<Integer>[] trimmedSets,
            int srchSpaceCol, 
            int trimCol, 
            SoftBarrier sBarrier,
            boolean countOnTheFly,
            boolean seek
    ) {
        setup(G, searchSpace, trimmedSets,
            srchSpaceCol, trimCol, sBarrier,
            countOnTheFly, seek);
    }
    
    public IterativeTrimProcessor(
            Graph G, 
            Set<Integer> searchSpace, 
            SplittableSinglyLinkedQueue<Integer> trimmedSet,
            int srchSpaceCol, 
            int trimCol, 
            SoftBarrier sBarrier,
            boolean countOnTheFly,
            boolean seek
    ){
        this.G = G;
        this.searchSpace = searchSpace;
        this.trimmedSet = trimmedSet;
        this.srchSpaceCol = srchSpaceCol;
        this.trimCol = trimCol;
        this.sBarrier = sBarrier;
        this.countOnTheFly = countOnTheFly;
        this.seek = seek;
    }

    public Vertex auxVertex;
    @Override
    public void processElement(Integer element) {
        auxVertex = G.vertices[element];
        
        if(countOnTheFly) auxVertex.nOut.set(countNeighbors(auxVertex.outNeighbours));
        if(auxVertex.nOut.get() == 0){
            trimThisOne(element);
            return;
        }
        
        if(countOnTheFly) auxVertex.nIn.set(countNeighbors(auxVertex.inNeighbours));
        
        if(auxVertex.nIn.get() == 0) trimThisOne(element);
    }
    
    int neighborCounter;
    public int countNeighbors(int[] neighborArray){
        neighborCounter = 0;
        for(int neighbor: neighborArray){
            if(checkColors(G.vertices[neighbor].color)) neighborCounter++;
        }
        return neighborCounter;
    }
    
    public boolean checkColors(int color){
        if(seek) return color == srchSpaceCol;
        else return color != srchSpaceCol;
    }
    
    private void trimThisOne(int vertexId){
        G.vertices[vertexId].color = trimCol;
        searchSpace.remove(vertexId);
        trimmedSet.enq(vertexId);
        if(FWBW.DEBUG2) {
            System.out.println("just trimmed vertex " + vertexId + " searchSpace size: " + searchSpace.size());
        }
        if(FWBW.TRIVIALCOUNTING)FWBW.nTrivialComponents.getAndIncrement(); // quasi output this SCC
    }

    @Override
    public ElementProcessor<Integer> clone(int cloneId) {
        return new IterativeTrimProcessor(
            G, 
            searchSpace, 
            trimmedSets[cloneId],
            srchSpaceCol, 
            trimCol, 
            sBarrier,
            countOnTheFly,
            seek
        );
    }

    @Override
    public final void setup(
            Graph G, Set<Integer> searchSpace, SplittableSinglyLinkedQueue<Integer>[] trimmedSets, 
            int srchSpaceCol, int trimCol, SoftBarrier sBarrier, boolean countOnTheFly, boolean seek) {
        this.G = G;
        this.searchSpace = searchSpace;
        this.trimmedSets = trimmedSets;
        this.trimmedSet = trimmedSets[0];
        this.srchSpaceCol = srchSpaceCol;
        this.trimCol = trimCol;
        this.sBarrier = sBarrier;
        this.countOnTheFly = countOnTheFly;
        this.seek = seek;
    }
}
