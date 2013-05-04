package rinde.sim.core.simulation.policies;

import rinde.sim.core.model.Agent;
import rinde.sim.core.model.InitUser;
import rinde.sim.core.simulation.TickPolicy;
import rinde.sim.core.simulation.time.TimeLapseHandle;

public interface AgentsPolicy extends TickPolicy{
    
    public void register(Agent agent, TimeLapseHandle handle);
    
    public void unregister(Agent agent);
    
    public void addInituser(InitUser user);
    
    /**
     * Returns an object which will determine how interactions
     * are processed. This can be used to handle interactions
     * deterministically or not.
     * @return The policy used when interactions occur.
     */
    public InteractionRules getInteractionRules();

    /**
     * Policies using thread pools get the chance to warm up
     * (initialize threads etc).
     */
    public void warmUp();
    
    /**
     * Let policies using thread pools shut down, kill all additional
     * threads.
     */
    public void shutDown();
}
