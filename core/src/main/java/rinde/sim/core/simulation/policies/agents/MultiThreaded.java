package rinde.sim.core.simulation.policies.agents;

import rinde.sim.core.simulation.TimeInterval;
import rinde.sim.core.simulation.policies.InteractionRules;
import rinde.sim.core.simulation.policies.agents.util.CustomPool;
import rinde.sim.core.simulation.policies.agents.util.Pool;
import rinde.sim.core.simulation.policies.agents.util.StdPool;

/**
 * Parallel time user policy which takes a batch of tick operations from multiple different
 * agents and threads that as an individual task. 
 * 
 * It avoids unnecessary context switching by using only (CORES - 1) threads +
 * the main thread to process tasks. 
 * 
 * @author dmerckx
 */
public class MultiThreaded extends AgentsPolicyAbstr{
    protected final Execution execution;
    protected final Pool pool;
    
    
    public MultiThreaded(Execution execution, Pool pool) {
        this.execution = execution;
        this.pool = pool;
        
        execution.setPool(pool);
    }

    @Override
    public void doTicks(TimeInterval interval) {
        execution.execute(agents.entrySet().iterator());
        
        //Afterwards he helps out with doing the work
        pool.helpFinish();
    }

    @Override
    public InteractionRules getInteractionRules() {
        return execution.getRules();
    }

    @Override
    public void warmUp() {
        pool.warmup();
    }

    @Override
    public void shutDown() {
        pool.shutDown();
    }
}