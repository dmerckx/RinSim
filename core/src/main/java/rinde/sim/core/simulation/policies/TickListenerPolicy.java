package rinde.sim.core.simulation.policies;

import rinde.sim.core.simulation.TickListener;
import rinde.sim.core.simulation.TimeInterval;

public class TickListenerPolicy extends SerialConcurrent<TickListener>{

    public TickListenerPolicy(boolean register) {
        super(register);
    }

    @Override
    public void doTick(TickListener obj, TimeInterval interval) {
        obj.tick(interval);
    }
}
