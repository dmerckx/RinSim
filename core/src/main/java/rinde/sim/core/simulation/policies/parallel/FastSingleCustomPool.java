package rinde.sim.core.simulation.policies.parallel;

import java.util.Map.Entry;
import java.util.concurrent.LinkedBlockingQueue;

import rinde.sim.core.model.Agent;
import rinde.sim.core.simulation.TimeInterval;
import rinde.sim.core.simulation.policies.InteractionRules;
import rinde.sim.core.simulation.time.TimeLapseHandle;

public class FastSingleCustomPool extends PTimeUserPolicy{
    protected int batchSize;
    protected Thread[] workers = new Thread[3];
    protected LinkedBlockingQueue<Task> tasks = new LinkedBlockingQueue<Task>();
    
    protected final Rules rules = createRules();
    
    public FastSingleCustomPool(int batchSize) {
        this(batchSize, NR_CORES-1);
    }
    
    public FastSingleCustomPool(int batchSize, int nrThreads) {
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
    
    protected Task makeTask(Agent agent, TimeLapseHandle handle, LatchNode node){
        return new RealSingleTask(agent, handle, node, rules);
    }

    @Override
    public void doTicks(TimeInterval interval) {
        LatchNode lastNode = new LatchNode();
        
        //The main thread start by dividing the work in pieces
        for(Entry<Agent, TimeLapseHandle> entry:agents.entrySet()){
            try {
                tasks.put(makeTask(entry.getKey(), entry.getValue(), lastNode));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            lastNode = lastNode.makeNext();
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

class RealSingleTask implements Task{
    protected final Agent agent;
    protected final TimeLapseHandle handle;
    protected final LatchNode node;
    protected final Rules rules;
    
    public RealSingleTask(Agent agent, TimeLapseHandle handle, LatchNode node, Rules rules) {
        this.agent = agent;
        this.handle = handle;
        this.node = node;
        this.rules = rules;
    }
    
    public void perform(){
        rules.previousLatch.set(node.getPrevious());
        
        agent.tick(handle);
        
        node.done();
    }
}