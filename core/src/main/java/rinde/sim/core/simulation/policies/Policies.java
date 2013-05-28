package rinde.sim.core.simulation.policies;

import rinde.sim.core.simulation.policies.agents.ModPoolBatch;
import rinde.sim.core.simulation.policies.agents.ModPoolBatchRecursive;
import rinde.sim.core.simulation.policies.agents.ModPoolSingle;
import rinde.sim.core.simulation.policies.agents.PoolBatch;
import rinde.sim.core.simulation.policies.agents.PoolSingle;

public class Policies {

    public static AgentsPolicy getClassicPool(int batchSize, int maxThreads){
        if(batchSize == 1)
            return new PoolSingle(maxThreads);
        
        return new PoolBatch(batchSize, maxThreads);
    }
    
    public static AgentsPolicy getModPool(int batchSize, int maxThreads, boolean recursive){
        if(batchSize == 1)
            return new ModPoolSingle(maxThreads-1);
        
        if(recursive)
            return new ModPoolBatchRecursive(batchSize, maxThreads);
        
        return new ModPoolBatch(batchSize, maxThreads);
    }
}
