package rinde.sim.core.simulation.policies;

import rinde.sim.core.simulation.policies.agents.MultiThreaded;
import rinde.sim.core.simulation.policies.agents.SingleThreaded;
import rinde.sim.core.simulation.policies.agents.util.CustomPool;
import rinde.sim.core.simulation.policies.agents.util.StdPool;
import rinde.sim.core.simulation.policies.execution.BatchExe;
import rinde.sim.core.simulation.policies.execution.BatchRecExe;
import rinde.sim.core.simulation.policies.execution.SingleExe;

public class Policies {

    public static AgentsPolicy getClassicPool(int maxThreads, int batchSize, boolean recursive){
        if(batchSize == 1)
            return new MultiThreaded(new SingleExe(), new StdPool(maxThreads));
        
        if(recursive)
            return new MultiThreaded(new BatchRecExe(batchSize), new StdPool(maxThreads));

        return new MultiThreaded(new BatchExe(batchSize), new StdPool(maxThreads));
    }
    
    public static AgentsPolicy getModPool(int maxThreads, int batchSize, boolean recursive){
        if(batchSize == 1)
            return new MultiThreaded(new SingleExe(), new CustomPool(maxThreads-1));
        
        if(recursive)
            return new MultiThreaded(new BatchRecExe(batchSize), new CustomPool(maxThreads-1));

        return new MultiThreaded(new BatchExe(batchSize), new CustomPool(maxThreads-1));
    }

    public static AgentsPolicy getSingleThreaded() {
        return new SingleThreaded();
    }
}
