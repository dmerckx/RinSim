package rinde.sim.core.simulation.policies.agents.util;

import java.util.concurrent.LinkedBlockingQueue;



public class Worker extends Thread {
    private CustomPool pool;
    
    public Worker(CustomPool pool) {
        this.pool = pool;
        setDaemon(true);
    }
    
    @Override
    public void run() {
        while(true){
            try {
                pool.doTask();
            } catch (InterruptedException e) {
                break;
            }
        }
    }
}