package rinde.sim.core.simulation.policies.agents;

import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import rinde.sim.core.model.Agent;
import rinde.sim.core.simulation.policies.InteractionRules;
import rinde.sim.core.simulation.policies.agents.util.LatchNode;
import rinde.sim.core.simulation.policies.agents.util.Pool;
import rinde.sim.core.simulation.time.TimeLapseHandle;

public abstract class Execution {
    
    protected Pool pool;
    
    public void setPool(Pool pool){
        this.pool = pool;
    }

    public abstract LatchNode execute(List<AgentContainer> agents);
    
    public abstract InteractionRules getRules();
}
