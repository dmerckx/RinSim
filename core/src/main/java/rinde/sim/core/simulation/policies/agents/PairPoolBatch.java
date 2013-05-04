package rinde.sim.core.simulation.policies.agents;

import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;

import rinde.sim.core.model.Agent;
import rinde.sim.core.simulation.policies.agents.util.LatchNode;
import rinde.sim.core.simulation.policies.agents.util.Rules;
import rinde.sim.core.simulation.time.TimeLapseHandle;


/**
 * 
 * @author dmerckx
 */
public class PairPoolBatch extends ModPoolBatch{
    
    public PairPoolBatch(int batchSize, int nrThreads) {
        super(batchSize, nrThreads);
    }
    
    @Override
    protected Rules createRules() {
        return new PairRules();
    }
    
    @Override
    protected Thread createWorker() {
        return new PairWorker(tasks);
    }
    
    @Override
    protected Task makeTask(List<Entry<Agent, TimeLapseHandle>> batch,
            LatchNode node) {
        return new PairTask(batch, node, rules);
    }
    
}

class PairTask extends Task{
    public PairTask(List<Entry<Agent,TimeLapseHandle>> batch, LatchNode node, Rules rules) {
        super(batch, node, rules);
    }
    
    public void performWithHelper(Helper helper){
        rules.node.set(node);
        ((PairRules) rules).helper.set(helper);
        
        for(Entry<Agent, TimeLapseHandle> e:batch){
            e.getKey().tick(e.getValue());
        }
        
        node.done();
    }
}


class PairWorker extends Thread {
    private LinkedBlockingQueue<Runnable> tasks;
    private Helper helper;
    
    public PairWorker(LinkedBlockingQueue<Runnable> tasks) {
        this.tasks = tasks;
        setDaemon(true);
        
        helper = new Helper(tasks);
        helper.start();
    }
    
    @Override
    public void run() {
        while(true){
            try {
                Runnable task = tasks.take();
                
                if(task instanceof PairTask)
                    ((PairTask) task).performWithHelper(helper);
                else
                    task.run();
            } catch (InterruptedException e) {
                break;
            } 
        }
    }
}

class Helper extends Thread {
    private CountDownLatch latch = new CountDownLatch(1);
    private LinkedBlockingQueue<Runnable> tasks;
    
    public Helper(LinkedBlockingQueue<Runnable> tasks) {
        this.tasks = tasks;
        setDaemon(true);
    }
    
    public void requestHelp(){
        latch.countDown();
    }
    
    @Override
    public void run() {
        while(true){
            try {
                latch.await();
            } catch (InterruptedException e1) {
                break;  //end thread
            }

            Runnable task = tasks.poll();
            if(task != null) task.run();
            
            latch = new CountDownLatch(1);
        }
    }
}

class PairRules extends Rules{
    public final ThreadLocal<Helper> helper = new ThreadLocal<Helper>();

    @Override
    public void awaitAllPrevious() {
        if( node.get() == null)
            return;

        try {
            if(node.get().isReady()){
                Helper h = helper.get();
                if(h != null) h.requestHelp();
            }
            node.get().await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}