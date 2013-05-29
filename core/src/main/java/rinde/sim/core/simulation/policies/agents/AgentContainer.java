package rinde.sim.core.simulation.policies.agents;

import rinde.sim.core.model.Agent;
import rinde.sim.core.simulation.time.TimeLapseHandle;

public class AgentContainer {
    public final Agent agent;
    public final TimeLapseHandle handle;
    
    public AgentContainer(Agent agent, TimeLapseHandle handle) {
        this.agent = agent;
        this.handle = handle;
    }
    
    public void doTick(){
        agent.tick(handle);
    }
}
