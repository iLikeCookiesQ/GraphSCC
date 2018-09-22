/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package DataStructures;

import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author Benjamin
 */
public class Vertex {
    public int[] outNeighbours;
    public int[] inNeighbours;
    public int color;
    public final AtomicInteger nOut, nIn; // used for trimming: in and out neighbor count
    public Vertex(int out, int in){
        this.outNeighbours = new int[out];
        this.inNeighbours = new int[in];
        this.color = 0;   
        this.nOut = new AtomicInteger(out);
        this.nIn = new AtomicInteger(in);
    }
}
