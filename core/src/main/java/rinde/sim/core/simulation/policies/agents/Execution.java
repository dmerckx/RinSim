package rinde.sim.core.simulation.policies.agents;

import java.util.List;

import rinde.sim.core.simulation.policies.InteractionRules;
import rinde.sim.core.simulation.policies.agents.util.LatchNode;
import rinde.sim.core.simulation.policies.agents.util.Pool;

public abstract class Execution {
    
    protected Pool pool;
    
    public void setPool(Pool pool){
        this.pool = pool;
    }

    public abstract LatchNode execute(LatchNode startingNode, List<AgentContainer> agents);
    
    public abstract InteractionRules getRules();
}
