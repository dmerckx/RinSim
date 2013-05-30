package rinde.sim.core.simulation.policies.agents.util;

import rinde.sim.core.simulation.policies.InteractionRules;


public class Rules implements InteractionRules {
    public final ThreadLocal<LatchNode> node = new ThreadLocal<LatchNode>();
    
    private int queries = 0;
    
    @Override
    public void awaitAllPrevious() {
        /*if( node.get() == null)
            return;

        try {
            node.get().awaitPrevious();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/
    }

    @Override
    public boolean isDeterministic() {
        return true;
    }

    @Override
    public synchronized void notifyQuery(double range) {
        queries++;
    }
    
    public int getQueryCount(){
        return queries;
    }
    
    public synchronized void resetQueryCount(){
        queries = 0;
    }
}
