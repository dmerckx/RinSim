package rinde.sim.core.simulation.policies.agents.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StdPool extends Pool{
    
    private final ExecutorService pool;
    
    public StdPool(int nrThreads) {
        super(nrThreads);
        pool = Executors.newFixedThreadPool(nrThreads);
    }
    
    public void addTask(final Runnable task){
        pool.execute(task);
    }
    
    public void shutDown(){
        pool.shutdown();
    }
    
    @Override
    public String toString() {
        return cores + "S";
    }
}