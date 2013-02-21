package rinde.sim.core.simulation.policies.parallel;

import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;

import rinde.sim.core.model.Agent;
import rinde.sim.core.simulation.time.TimeLapseHandle;


/**
 * Parallel time user policy which takes a batch of tick operations from multiple different
 * agents and threads that as an individuel task.
 * 
 * @author dmerckx
 */
public class CustomPairPool extends CustomPool{
    
    public CustomPairPool(int batchSize) {
        super(batchSize);
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
        rules.previousLatch.set(node.getPrevious());
        ((PairRules) rules).helper.set(helper);
        
        for(Entry<Agent, TimeLapseHandle> e:batch){
            e.getKey().tick(e.getValue());
        }
        
        node.done();
    }
}


class PairWorker extends Thread {
    private LinkedBlockingQueue<Task> tasks;
    private Helper helper;
    
    public PairWorker(LinkedBlockingQueue<Task> tasks) {
        this.tasks = tasks;
        setDaemon(true);
        
        helper = new Helper(tasks);
        helper.start();
    }
    
    @Override
    public void run() {
        while(true){
            try {
                ((PairTask) tasks.take()).performWithHelper(helper);
            } catch (InterruptedException e) {
                break;
            } 
        }
    }
}

class Helper extends Thread {
    private CountDownLatch latch = new CountDownLatch(1);
    private LinkedBlockingQueue<Task> tasks;
    
    public Helper(LinkedBlockingQueue<Task> tasks) {
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

            Task task = tasks.poll();
            if(task != null) task.perform();
            
            latch = new CountDownLatch(1);
        }
    }
}

class PairRules extends Rules{
    public final ThreadLocal<Helper> helper = new ThreadLocal<Helper>();

    @Override
    public void awaitAllPrevious() {
        if( previousLatch.get() == null)
            return;

        try {
            if(previousLatch.get().getCount() != 0){
                Helper h = helper.get();
                if(h != null) h.requestHelp();
            }
            previousLatch.get().await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}