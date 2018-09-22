/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GraphStuff;
import DataStructures.Vertex;
import DataStructures.Graph;
import java.io.File;
import java.util.Scanner;

/**
 * This class takes an input file and sets up the appropriate data structures
 * for SCC algorithms.
 * @author Benjamin
 */
public class GraphLoader {
    // TODO: make filename a command line argument.
    final boolean DEBUG = false;
    String tgtFile;
    GraphLoader(String s){
        tgtFile = s;
    }
    
    Graph loadGraph() throws Exception {
        Graph G;
        File inFile = new File(tgtFile);
        if(!inFile.isFile()){
            System.out.println("Specified file doesn't exist.");
            System.exit(0);
        }
        
        Scanner read = new Scanner(inFile);
        Scanner read2;
        String s = read.nextLine();
        s = read.nextLine();
        //System.out.println(s);
        int nVertices = Integer.parseInt(s.substring(20));
        G = new Graph(nVertices);
        int nOutNeighbours, nInNeighbours;
        if(DEBUG) System.out.println("nVertices: " + nVertices);
        for(int currentVertex = 0; currentVertex < nVertices; currentVertex++){          
            s = read.next();
            s = read.next();
            s = read.next();
            nOutNeighbours = read.nextInt();
            nInNeighbours = read.nextInt();
                 
            if(DEBUG) System.out.println("currentVertex: " + currentVertex);
            if(DEBUG) System.out.println("nOutNeighbours: " + nOutNeighbours + 
                    " nInNeighbours: " + nInNeighbours);
            
            G.vertices[currentVertex] = new Vertex(nOutNeighbours, nInNeighbours);
            
            for(int i = 0; i < nOutNeighbours; i++){
                G.vertices[currentVertex].outNeighbours[i] = read.nextInt();
                if(DEBUG) System.out.print(G.vertices[currentVertex].outNeighbours[i] + " ");
            }
            
            if(DEBUG) System.out.print("\n");
            
            for(int i = 0; i < nInNeighbours; i++){
                G.vertices[currentVertex].inNeighbours[i] = read.nextInt();
                if(DEBUG)System.out.print(G.vertices[currentVertex].inNeighbours[i] + " ");
            }
            
            if(DEBUG) System.out.println("hello?" + read.hasNext() + "\n");
        }
        return G;
    }
}
