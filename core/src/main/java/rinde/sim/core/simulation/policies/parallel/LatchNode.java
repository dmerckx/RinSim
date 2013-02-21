package rinde.sim.core.simulation.policies.parallel;

import java.util.concurrent.CountDownLatch;

public class LatchNode{
    private final LatchNode previousNode;
    private LatchNode nextNode = null;
    
    public CountDownLatch latch;
    public boolean ready;
    public boolean beforeIsDone;
    
    public LatchNode(LatchNode previous){
        previousNode = previous;
        
        latch = new CountDownLatch(1);
        ready = false;
        beforeIsDone = false;
    }
    
    public LatchNode(){
        previousNode = null;
        
        latch = new CountDownLatch(1);
        ready = false;
        beforeIsDone = true;
    }
    
    public synchronized LatchNode makeNext(){
        nextNode = new LatchNode(this);
        if(ready && beforeIsDone){
            latch.countDown();
            nextNode.beforeIsDone();
        }
        
        return nextNode;
    }
    
    public synchronized void done(){
        ready = true;
        
        //If the node before is done
        //  -> this node is finished aswell
        //  -> notify the next node
        if(beforeIsDone){
            latch.countDown();
            if(nextNode != null)
                nextNode.beforeIsDone();
        }
    }
    
    public synchronized void beforeIsDone(){
        beforeIsDone = true;
        
        //If this node was ready
        //  -> this node is finished
        //  -> notify the next node
        if(ready){
            latch.countDown();
            if(nextNode != null)
                nextNode.beforeIsDone();
        }
    }
    
    public void await() throws InterruptedException{
        latch.await();
    }
    
    public CountDownLatch getPrevious(){
        return previousNode == null? new CountDownLatch(0) : previousNode.latch;
    }
    
    public void awaitPrevious() throws InterruptedException{
        if(previousNode != null)
            previousNode.await();
    }
}