package rinde.sim.core.simulation.policies.agents;

import rinde.sim.core.simulation.TimeInterval;
import rinde.sim.core.simulation.policies.InteractionRules;
import rinde.sim.util.Rectangle;

public class SingleThreaded extends AgentsPolicyAbstr{
    @Override
    public InteractionRules getInteractionRules() {
        return new InteractionRules() {
            @Override
            public boolean isDeterministic() {
                return true;
            }
            
            @Override
            public void awaitAllPrevious() {
                //Nothing has to be done since execution is single threaded
            }

            @Override
            public void notifyQuery(double range) {
            }
        };
    }

    @Override
    public void warmUp() {
        
    }

    @Override
    public void shutDown() {
        
    }

    @Override
    protected void doTicks(TimeInterval interval) {
        for(AgentContainer c:agents){
            c.doTick();
        }
    }

    @Override
    public void init(Rectangle mapSize) {
        
    }

}
