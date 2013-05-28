package rinde.sim.core.simulation.policies;


public interface InteractionRules {
    void notifyQuery(double range);
    
    void awaitAllPrevious();
    
    boolean isDeterministic();
} 
