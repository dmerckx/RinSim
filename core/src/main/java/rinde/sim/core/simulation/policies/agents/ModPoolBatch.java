package rinde.sim.core.simulation.policies.agents;

import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.LinkedBlockingQueue;

import rinde.sim.core.model.Agent;
import rinde.sim.core.simulation.TimeInterval;
import rinde.sim.core.simulation.policies.InteractionRules;
import rinde.sim.core.simulation.policies.agents.util.LatchNode;
import rinde.sim.core.simulation.policies.agents.util.Rules;
import rinde.sim.core.simulation.policies.agents.util.WarmupTask;
import rinde.sim.core.simulation.policies.agents.util.Worker;
import rinde.sim.core.simulation.time.TimeLapseHandle;

import com.google.common.collect.Lists;

/**
 * Parallel time user policy which takes a batch of tick operations from multiple different
 * agents and threads that as an individual task. 
 * 
 * It avoids unnecessary context switching by using only (CORES - 1) threads +
 * the main thread to process tasks. 
 * 
 * @author dmerckx
 */
public class ModPoolBatch extends AgentsPolicyAbstr{
    protected int batchSize;
    protected Thread[] workers;
    protected LinkedBlockingQueue<Runnable> tasks = new LinkedBlockingQueue<Runnable>();
    
    protected final Rules rules = createRules();
    
    public ModPoolBatch(int batchSize) {
        this(batchSize, NR_CORES-1);
    }
    
    public ModPoolBatch(int batchSize, int nrThreads) {
        this.batchSize = batchSize;
        
        workers = new Thread[nrThreads];
        tasks = new LinkedBlockingQueue<Runnable>();
        
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
    
    protected Runnable makeTask(List<Entry<Agent,TimeLapseHandle>> batch, LatchNode node){
        return new Task(batch, node, rules);
    }

    @Override
    public void doTicks(TimeInterval interval) {
        LatchNode lastNode = new LatchNode();
        Iterator<Entry<Agent, TimeLapseHandle>> it = agents.entrySet().iterator();
        
        int c = 0;
        List<Entry<Agent,TimeLapseHandle>> batch = Lists.newArrayListWithCapacity(batchSize);
        
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
            Runnable task = tasks.poll();
            if(task != null) task.run(); 
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
            Runnable task = tasks.poll();
            if(task != null) task.run(); 
        }
    }

    @Override
    public void shutDown() {
        for(Thread worker:workers){
            worker.interrupt();
        }
    }
}

class Task implements Runnable{
    protected final List<Entry<Agent, TimeLapseHandle>> batch;
    protected final LatchNode node;
    protected final Rules rules;
    
    public Task(List<Entry<Agent,TimeLapseHandle>> batch, LatchNode node, Rules rules) {
        this.batch = batch;
        this.node = node;
        this.rules = rules;
    }
    
    public void run(){
        rules.node.set(node);
        
        for(Entry<Agent, TimeLapseHandle> e:batch){
            e.getKey().tick(e.getValue());
        }
        
        node.done();
    }
}