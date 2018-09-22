/*
 * This contains the description of the graph object
 * 
 */
package DataStructures;

/**
 *
 * @author Benjamin
 */
public class Graph{
    /* 
     */
    // Set holding the integer identifiers of each vertex
    public Vertex[] vertices;
    public Graph(int nVertices){
        this.vertices = new Vertex[nVertices];
    }
}
