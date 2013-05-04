package rinde.sim.core.simulation.policies.agents.util;

import java.util.concurrent.LinkedBlockingQueue;



public class Worker extends Thread {
    private LinkedBlockingQueue<Runnable> tasks;
    
    public Worker(LinkedBlockingQueue<Runnable> tasks) {
        this.tasks = tasks;
        setDaemon(true);
    }
    
    @Override
    public void run() {
        while(true){
            try {
                tasks.take().run();
            } catch (InterruptedException e) {
                break;
            } 
        }
    }
}