package rinde.sim.core.simulation.policies;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;

import rinde.sim.core.simulation.TickPolicy;
import rinde.sim.core.simulation.TimeInterval;
import rinde.sim.core.simulation.TimeLapse;
import rinde.sim.core.simulation.time.TimeLapseGroup;
import rinde.sim.core.simulation.types.Agent;
import rinde.sim.core.simulation.types.AgentPort;

public class ParallelAgents extends ThreadState implements TickPolicy<Agent>{

    private HashMap<Agent, List<AgentPort>> listeners = new HashMap<Agent, List<AgentPort>>();
    final TimeLapseGroup group = new TimeLapseGroup();
    
    @Override
    public Class<Agent> getAcceptedType() {
        return Agent.class;
    }

    @Override
    public void register(Agent listener) {
        if( listener instanceof AgentPort ){
            Agent agent = ((AgentPort) listener).getAgent();
            
            if( !listeners.containsKey(agent))
                throw new IllegalStateException(
                        "AgentPort.getAgent() is not registered first");
            
            listeners.get(agent).add((AgentPort) listener);
        }
        else{
            listeners.put(listener, new ArrayList<AgentPort>());
        }
    }

    @Override
    public void unregister(Agent listener) {
        if( listener instanceof AgentPort ){
            Agent agent = ((AgentPort) listener).getAgent();
            
            if( !listeners.containsKey(agent))
                throw new IllegalStateException(
                        "AgentPort.getAgent() is not registered");
            
            listeners.get(agent).remove(listener);
        }
        else{
            listeners.remove(listener);
        }
    }

    @Override
    public void performTicks(final TimeInterval interval) {
        List<Future<?>> futures = new ArrayList<Future<?>>();
        List<CountDownLatch> barriers = new ArrayList<CountDownLatch>();
        
        for(final Agent agent:listeners.keySet()){
            final List<AgentPort> ports = listeners.get(agent);
            final ThreadLocal<CountDownLatch> local = previousBarrier;
            final CountDownLatch barrier = new CountDownLatch(1);
            barriers.add(barrier);
            
            futures.add(pool.submit(new Runnable() {
                @Override
                public void run() {
                    local.set(barrier);
                    TimeLapse lapse = group.forge(interval);
                    
                    for(AgentPort port:ports){
                        port.tick(lapse);
                    }
                    agent.tick(lapse);
                    
                    local.set(null);
                }
            }));
        }
        
        for(int i = 0; i < futures.size(); i++){
            try {
                futures.get(i).get();
            } catch (Exception e) {
                e.printStackTrace();
            }
            barriers.get(i).countDown();
        }
    }

    @Override
    public boolean canRegisterDuringExecution() {
        return false;
    }

    @Override
    public boolean canUnregisterDuringExecution() {
        return false;
    }

}
