package rinde.sim.core.simulation.policies.parallel;

import java.util.concurrent.CountDownLatch;

import rinde.sim.core.simulation.policies.InteractionRules;


class Rules implements InteractionRules {
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
}
