package rinde.sim.core.simulation.policies.agents.util;

import rinde.sim.core.simulation.policies.InteractionRules;


public class Rules implements InteractionRules {
    public final ThreadLocal<LatchNode> node = new ThreadLocal<LatchNode>();
    
    @Override
    public void awaitAllPrevious() {
        if( node.get() == null)
            return;

        try {
            node.get().awaitPrevious();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isDeterministic() {
        return true;
    }
}
