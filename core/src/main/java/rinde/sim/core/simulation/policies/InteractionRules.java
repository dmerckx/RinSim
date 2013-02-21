package rinde.sim.core.simulation.policies;


public interface InteractionRules {
    
    void awaitAllPrevious();
    
    boolean isDeterministic();
} 
