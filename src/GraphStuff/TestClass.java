/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GraphStuff;

import java.util.HashMap;
import java.util.Random;
import java.util.TreeSet;

/**
 * Another messy test class that isn't used for anything but testing code.
 * @author Benjamin
 */
public class TestClass {
    static int N = 1000000;
    static double bucketWidth = 0.25;
    static int assignBucket(double d){
        return (int)Math.ceil((d-0.5*bucketWidth)/bucketWidth);
    }
    
    static HashMap<Integer, Integer> hashMap = new HashMap();
    static void updateMap(int bucket){
        if(!hashMap.containsKey(bucket)) hashMap.put(bucket, 1);
        else {
            hashMap.put(bucket, hashMap.get(bucket) + 1);
        }
    }
    
    public static void main(String[] args){
        //ProbabilityFunction pf = new NormallyDistributedBonus(N, 1.0);
        /*ProbabilityFunction pf = new NoBonus();
        ProbabilityFunction pf2 = new LinearRampBonus(N, 1.0);
        
        for(int i = 0; i < N; i++){
            updateMap(assignBucket(pf.getBonus(i, 0) + pf2.getBonus(i, 0)));
        }
        
        TreeSet<Integer> set = new TreeSet(hashMap.keySet());
        int lineCounter = 0;
        for(int bucket: set){
            System.out.print(bucket + ": " + (hashMap.get(bucket)) + "\t");
            if(++lineCounter%10 == 0) System.out.print("\n");
        }//*/
    }
}
