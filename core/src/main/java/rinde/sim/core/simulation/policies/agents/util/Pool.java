package rinde.sim.core.simulation.policies.agents.util;

public abstract class Pool {
    protected int busyTasks;
    
    public Pool() {
        busyTasks = 0;
    }
    
    synchronized void countUp(){
        busyTasks++;
    }
    
    synchronized void countDown(){
        busyTasks--;
        if(busyTasks == 0) this.notifyAll();
    }
    
    public abstract void addTask(Runnable task);
    
    public void helpFinish(){
        synchronized (this) {
            if(busyTasks == 0) return;
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    
    public void warmup(){
        long before = System.currentTimeMillis();
        for(int i = 0; i < 6000; i++){
            addTask(new WarmupTask());
        }
        helpFinish();
    }
    
    public abstract void shutDown();
}
