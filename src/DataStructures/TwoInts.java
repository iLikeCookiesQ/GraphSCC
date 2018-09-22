/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package DataStructures;

import java.util.Comparator;

/**
 * Used to associate a vertexId with another statistic. 
 * Used to store a vertex' product of in- and outdegrees, as well as to store
 * colors of special roots.
 * 
 * 
 * @author Benjamin
 */
    // 
    public class TwoInts implements Comparable<TwoInts>, Comparator<TwoInts>{
        public int vertexId;
        public int statistic;
        
        public TwoInts(){
            
        }
        public TwoInts(int vertexId, int statistic){
            this.vertexId = vertexId;
            this.statistic = statistic;
        }
        
        private int difference;
        @Override
        public int compare(TwoInts o1, TwoInts o2){
            difference = o1.statistic - o2.statistic;
            if(difference != 0) return difference;
            else return o1.vertexId - o2.vertexId;
        }

        @Override
        public int compareTo(TwoInts o){
            difference = this.statistic - o.statistic;
            if(difference != 0) return difference;
            else return this.vertexId - o.vertexId;
        }
    }
