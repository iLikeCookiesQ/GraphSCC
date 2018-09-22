/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GraphStuff.FWBW;

import java.util.Set;

/**
 * This class wraps the FWBW class in a Runnable to support parallelism in
 * recursion.
 * @author Benjamin
 */
public class FWBWRunnable extends FWBW implements Runnable{
    final Set<Integer> searchSpace;
    final int srchSpaceCol;
    final boolean isWorkerThread;
    public FWBWRunnable(
            Set<Integer> searchSpace,
            int srchSpaceCol,
            boolean isWorker
    ){       
        this.searchSpace = searchSpace;
        this.srchSpaceCol = srchSpaceCol;
        this.isWorkerThread = isWorker;
    }
    
    @Override
    public void run(){
        try{
            runFWBW(searchSpace, srchSpaceCol);
        }catch(Exception e){System.out.println(e);System.exit(0);}
        finally{
            if(isWorkerThread){
                checkOutThread();
            }
        }
    }
}
