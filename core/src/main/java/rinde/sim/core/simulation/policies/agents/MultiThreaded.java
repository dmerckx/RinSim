package rinde.sim.core.simulation.policies.agents;

import java.util.List;

import com.google.common.collect.Lists;

import rinde.sim.core.model.Agent;
import rinde.sim.core.model.road.AbstractRoadModel;
import rinde.sim.core.model.road.apis.RoadGuard;
import rinde.sim.core.model.road.users.RoadUser;
import rinde.sim.core.simulation.TimeInterval;
import rinde.sim.core.simulation.policies.InteractionRules;
import rinde.sim.core.simulation.policies.agents.util.LatchNode;
import rinde.sim.core.simulation.policies.agents.util.Pool;
import rinde.sim.core.simulation.policies.execution.AdaptiveBatchRecExe;
import rinde.sim.util.positions.ConcurrentRegion;

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
        long start = System.nanoTime();
        
        LatchNode node = new LatchNode();
        
        /*for(ConcurrentRegion<?> c:AbstractRoadModel.cache.regions){
            List<AgentContainer> containers = Lists.newArrayList();
            for(RoadUser<?> a:c.values){
                if(a instanceof Agent)
                containers.add(new AgentContainer((Agent) a, ((RoadGuard) a.getRoadState()).getHandle()));
            }
            node = execution.execute(node, containers);
        }*/
        node = execution.execute(node, agents);
        
        //Afterwards he helps out with doing the work
        pool.awaitFinish();
        
        node.done();
        try {
            node.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        if(execution instanceof AdaptiveBatchRecExe){
            ((AdaptiveBatchRecExe) execution).setTime(System.nanoTime() - start);
        }
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
    
    @Override
    public String toString() {
        return pool + " " + execution;
    }
}