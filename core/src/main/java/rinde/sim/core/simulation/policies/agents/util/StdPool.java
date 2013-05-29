package rinde.sim.core.simulation.policies.agents.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StdPool extends Pool{
    
    private final ExecutorService pool;
    
    public StdPool(int nrThreads) {
        pool = Executors.newFixedThreadPool(nrThreads);
    }
    
    public void addTask(final Runnable task){
        countUp();
        pool.execute(new Runnable() {
            @Override
            public void run() {
                task.run();
                countDown();
            }
        });
    }
    
    public void shutDown(){
        pool.shutdown();
    }
}