package rinde.sim.core.simulation.policies.parallel;

import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.LinkedBlockingQueue;

import rinde.sim.core.model.Agent;
import rinde.sim.core.simulation.TimeInterval;
import rinde.sim.core.simulation.policies.InteractionRules;
import rinde.sim.core.simulation.time.TimeLapseHandle;

import com.google.common.collect.Lists;

/**
 * Parallel time user policy which takes a batch of tick operations from multiple different
 * agents and threads that as an individual task.
 * 
 * @author dmerckx
 */
public class CustomPool extends PTimeUserPolicy{
    protected int batchSize;
    protected Thread[] workers = new Thread[3];
    protected LinkedBlockingQueue<Task> tasks = new LinkedBlockingQueue<Task>();
    
    protected final Rules rules = createRules();
    
    public CustomPool(int batchSize) {
        this(batchSize, NR_CORES-1);
    }
    
    public CustomPool(int batchSize, int nrThreads) {
        this.batchSize = batchSize;
        
        workers = new Thread[nrThreads];
        tasks = new LinkedBlockingQueue<Task>();
        
        for(int i = 0; i < workers.length; i++){
            workers[i] = createWorker();
            workers[i].start();
        }
    }
    
    protected Rules createRules(){
        return new Rules();
    }
    
    protected Thread createWorker(){
        return new Worker(tasks);
    }
    
    protected Task makeTask(List<Entry<Agent,TimeLapseHandle>> batch, LatchNode node){
        return new RealTask(batch, node, rules);
    }

    @Override
    public void doTicks(TimeInterval interval) {
        LatchNode lastNode = new LatchNode();
        Iterator<Entry<Agent, TimeLapseHandle>> it = agents.entrySet().iterator();
        
        int c = 0;
        List<Entry<Agent,TimeLapseHandle>> batch = Lists.newArrayList();
        
        //The main thread start by dividing the work in pieces
        while(it.hasNext()){
            Entry<Agent, TimeLapseHandle> entry = it.next();
            batch.add(entry);
            c = (c + 1) % batchSize;
            
            if(c == 0 || !it.hasNext()){
                try {
                    tasks.put(makeTask(batch, lastNode));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                
                lastNode = lastNode.makeNext();
                batch = Lists.newArrayList();
            }
        }
        
        //Afterwards he helps out with doing the work
        while(!tasks.isEmpty()){
            Task task = tasks.poll();
            if(task != null) task.perform(); 
        }
        
        //Wait for all the tasks to be finished
        try {
            lastNode.done();
            lastNode.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public InteractionRules getInteractionRules() {
        return rules;
    }

    @Override
    public void warmUp() {
        long before = System.currentTimeMillis();
        
        for(int i = 0; i < 4000; i++){
            tasks.add(new WarmupTask());
        }
        while(!tasks.isEmpty()){
            Task task = tasks.poll();
            if(task != null) task.perform(); 
        }
    }

    @Override
    public void shutDown() {
        for(Thread worker:workers){
            worker.interrupt();
        }
    }
}

interface Task{
    public void perform();
}

class WarmupTask implements Task{
    public String warmup;
    
    @Override
    public void perform() {
        for(int i = 0; i < 500; i++){
            warmup += i + "j*";
        }
    }
}

class RealTask implements Task{
    protected final List<Entry<Agent, TimeLapseHandle>> batch;
    protected final LatchNode node;
    protected final Rules rules;
    
    public RealTask(List<Entry<Agent,TimeLapseHandle>> batch, LatchNode node, Rules rules) {
        this.batch = batch;
        this.node = node;
        this.rules = rules;
    }
    
    public void perform(){
        rules.previousLatch.set(node.getPrevious());
        
        for(Entry<Agent, TimeLapseHandle> e:batch){
            e.getKey().tick(e.getValue());
        }
        
        node.done();
    }
}

class Worker extends Thread {
    private LinkedBlockingQueue<Task> tasks;
    
    public Worker(LinkedBlockingQueue<Task> tasks) {
        this.tasks = tasks;
        setDaemon(true);
    }
    
    @Override
    public void run() {
        while(true){
            try {
                tasks.take().perform();
            } catch (InterruptedException e) {
                break;
            } 
        }
    }
}