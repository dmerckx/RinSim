package rinde.sim.core.simulation.policies.agents.util;

import java.util.concurrent.atomic.AtomicInteger;

public abstract class Pool {
    protected final int cores;
    public Pool(int cores) {
        this.cores = cores;
    }
    
    public abstract void addTask(Runnable task);
    
    public void helpFinish(){}
    
    public void warmup(){
        long before = System.currentTimeMillis();
        for(int i = 0; i < 6000; i++){
            addTask(new WarmupTask());
        }
        helpFinish();
    }
    
    public abstract void shutDown();
}
