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
 * {@link ModPoolBatch} with recursive execution
 * @author dmerckx
 */
public class ModPoolBatch2 extends AgentsPolicyAbstr{
    protected int batchSize;
    protected Thread[] workers;
    protected LinkedBlockingQueue<Runnable> tasks = new LinkedBlockingQueue<Runnable>();
    
    protected final Rules2 rules = createRules();
    
    public ModPoolBatch2(int batchSize) {
        this(batchSize, NR_CORES-1);
    }
    
    public ModPoolBatch2(int batchSize, int nrThreads) {
        this.batchSize = batchSize;
        
        workers = new Thread[nrThreads];
        tasks = new LinkedBlockingQueue<Runnable>();
        
        for(int i = 0; i < workers.length; i++){
            workers[i] = createWorker();
            workers[i].start();
        }
    }
    
    protected Rules2 createRules(){
        return new Rules2();
    }
    
    protected Thread createWorker(){
        return new Worker(tasks);
    }
    
    protected Runnable makeTask(List<Entry<Agent,TimeLapseHandle>> batch, LatchNode node){
        return new Task2(batch, node, rules);
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

class Task2 implements Runnable{
    protected final List<Entry<Agent, TimeLapseHandle>> batch;
    protected final LatchNode node;
    protected final Rules2 rules;
    
    private int counter;
    
    public Task2(List<Entry<Agent,TimeLapseHandle>> batch, LatchNode node, Rules2 rules) {
        this.batch = batch;
        this.node = node;
        this.rules = rules;
        this.counter = 0;
    }
    
    public void run(){
        rules.node.set(node);
        rules.task.set(this);
        
        startNext();
        
        node.done();
    }
    
    public void startNext(){
        while(counter < batch.size()){
            counter++;
            Entry<Agent, TimeLapseHandle> e = batch.get(counter-1);
            e.getKey().tick(e.getValue());
        }
    }
}

class Rules2 extends Rules {
    public final ThreadLocal<Task2> task = new ThreadLocal<Task2>();
    
    @Override
    public void awaitAllPrevious() {
        if( node.get() == null)
            return;
        
        task.get().startNext();
        try {
            node.get().awaitPrevious();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isDeterministic() {
        return true;
    }
}