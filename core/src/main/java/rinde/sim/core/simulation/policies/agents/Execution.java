package rinde.sim.core.simulation.policies.agents;

import java.util.Iterator;
import java.util.Map.Entry;

import rinde.sim.core.model.Agent;
import rinde.sim.core.simulation.policies.InteractionRules;
import rinde.sim.core.simulation.policies.agents.util.Pool;
import rinde.sim.core.simulation.time.TimeLapseHandle;

public abstract class Execution {
    
    protected Pool pool;
    
    public void setPool(Pool pool){
        this.pool = pool;
    }

    public abstract void execute(Iterator<Entry<Agent, TimeLapseHandle>> agents);
    
    public abstract InteractionRules getRules();
}
