package rinde.sim.core.simulation.policies.agents.util;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;

public class CustomPool extends Pool{
    private LinkedBlockingQueue<Runnable> tasks;
    protected Thread[] workers;
    
    public CustomPool(int nrThreads) {
        workers = new Thread[nrThreads];
        tasks = new LinkedBlockingQueue<Runnable>();
        
        for(int i = 0; i < workers.length; i++){
            workers[i] = new Worker(this);
            workers[i].start();
        }
    }
    
    public void doTask() throws InterruptedException{
        tasks.take().run(); //blocks
        countDown();
    }
    
    public void addTask(Runnable task){
        countUp();
        try {
            tasks.put(task);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    
    public void helpFinish(){
        while(true){
            Runnable task = tasks.poll();
            if(task == null) break;
            task.run();
            countDown();
        }
        super.helpFinish();
    }
    
    public void shutDown(){
        for(Thread worker:workers){
            worker.interrupt();
        }
    }
}

class FinishTask implements Runnable {
    private final CountDownLatch latch;
    
    public FinishTask(int threads) {
        latch = new CountDownLatch(threads);
    }
    
    public void run() {
        latch.countDown();
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}