package rinde.sim.core.simulation;

public interface Pool {

    void addTask(Runnable task);
    
    void await();
    
}
