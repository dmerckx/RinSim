package rinde.sim.core.simulation.policies;

import rinde.sim.core.simulation.TickListener;
import rinde.sim.core.simulation.TimeInterval;

public class TickListenerSerialPolicy extends Serial<TickListener> implements TickListenerPolicy{
    
    public TickListenerSerialPolicy(boolean register) {
        super(register);
    }

    @Override
    public void doTick(TickListener obj, TimeInterval interval) {
        obj.tick(interval);
    }
}
