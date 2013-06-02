package rinde.sim.core.simulation.policies.agents.util;

import java.util.concurrent.LinkedBlockingQueue;

public class CustomPool extends Pool{
    private LinkedBlockingQueue<Runnable> tasks;
    protected Thread[] workers;
    
    public CustomPool(int nrThreads) {
        super(nrThreads+1);
        workers = new Thread[nrThreads];
        tasks = new LinkedBlockingQueue<Runnable>();
        
        for(int i = 0; i < workers.length; i++){
            workers[i] = new Worker(this);
            workers[i].start();
        }
    }
    
    public void doTask() throws InterruptedException{
        tasks.take().run(); //blocks
    }
    
    public void addTask(Runnable task){
        try {
            tasks.put(task);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    
    public void awaitFinish(){
        while(true){
            Runnable task = tasks.poll();
            if(task == null) break;
            task.run();
        }
        super.awaitFinish();
    }
    
    public void shutDown(){
        for(Thread worker:workers){
            worker.interrupt();
        }
    }
    
    @Override
    public String toString() {
        return cores + "C";
    }
}