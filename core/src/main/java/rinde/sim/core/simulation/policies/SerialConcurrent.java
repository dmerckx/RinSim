package rinde.sim.core.simulation.policies;

import java.util.ArrayList;

import rinde.sim.core.simulation.TimeInterval;

public abstract class SerialConcurrent<T> extends Serial<T> {

    public SerialConcurrent(boolean register) {
        super(register);
    }

    @Override
    public void performTicks(TimeInterval interval) {
        for(T listener:new ArrayList<T>(listeners)){
            doTick(listener, interval);
        }
    }
}
