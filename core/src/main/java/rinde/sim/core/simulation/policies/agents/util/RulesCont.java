package rinde.sim.core.simulation.policies.agents.util;

import java.util.concurrent.CountDownLatch;

import rinde.sim.core.simulation.policies.InteractionRules;

public class RulesCont implements InteractionRules {
    public final ThreadLocal<CountDownLatch> previousLatch = new ThreadLocal<CountDownLatch>();
     
    @Override
    public void awaitAllPrevious() {
        if( previousLatch.get() == null)
            return;

        try {
            previousLatch.get().await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isDeterministic() {
        return true;
    }

    @Override
    public void notifyQuery(double range) {
        
    }
}
