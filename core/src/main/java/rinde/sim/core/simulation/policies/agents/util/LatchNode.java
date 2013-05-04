package rinde.sim.core.simulation.policies.agents.util;

import java.util.concurrent.CountDownLatch;

/**
 * Allows the creation of a chain of nodes.
 * Each node can be marked 'done' at some point in time.
 * A node is finished if it is marked done, and all the nodes
 * before him in the chain are marked done as well.
 * 
 * The node allows for users to await until it is finished.
 * 
 * The implementation is thread safe.
 * 
 * @author dmerckx
 */
public class LatchNode{
    private final LatchNode previousNode;
    private final CountDownLatch latch;
    
    private LatchNode nextNode = null;
    
    //-----State-----//
    private boolean done;   //This node is done
    private boolean beforeIsFinished;   //Previous node is finished
    public final int nr;
    
    /**
     * Create the first node of a chain, has no previous node.
     */
    public LatchNode(){
        previousNode = null;
        
        latch = new CountDownLatch(1);
        done = false;
        beforeIsFinished = true;
        nr = 0;
    }
    
    private LatchNode(LatchNode previous){
        previousNode = previous;
        
        latch = new CountDownLatch(1);
        done = false;
        beforeIsFinished = false;
        nr = previous.nr;
    }
    
    /**
     * Increase the chain of nodes by an extra node.
     * This method can only be called ones, otherwise a
     * tree could be formed instead of a chain.
     * @return A new node, with this node as his parent.
     */
    public synchronized LatchNode makeNext(){
        if(nextNode != null)
            throw new IllegalStateException("A chain can have only one child");
        
        nextNode = new LatchNode(this);
        if(done && beforeIsFinished){
            latch.countDown();
            nextNode.beforeIsDone();
        }
        
        return nextNode;
    }
    
    public boolean isReady(){
        return done && beforeIsFinished;
    }
    
    /**
     * Blocks until this node is finished.
     * A node is finish when it is marked done and all the nodes before
     * him are finished.
     * @throws InterruptedException When interrupted while waiting.
     */
    public void await() throws InterruptedException{
        latch.await();
    }
    
    /**
     * Blocks until all the previous nodes are finished.
     * @throws InterruptedException When interrupted while waiting.
     */
    public void awaitPrevious() throws InterruptedException{
        if(previousNode != null){
            previousNode.await();
        }
    }
    
    /**
     * Mark this node to be done.
     */
    public synchronized void done(){
        done = true;
        
        //If the node before is done
        //  -> this node is finished as well
        //  -> notify the next node
        if(beforeIsFinished){
            latch.countDown();
            if(nextNode != null)
                nextNode.beforeIsDone();
        }
    }
    
    private synchronized void beforeIsDone(){
        beforeIsFinished = true;
        
        //If this node was ready
        //  -> this node is finished
        //  -> notify the next node
        if(done){
            latch.countDown();
            if(nextNode != null)
                nextNode.beforeIsDone();
        }
    }
}