package rinde.sim.core.simulation.policies;

import rinde.sim.core.simulation.TickListener;
import rinde.sim.core.simulation.TimeInterval;
import rinde.sim.core.simulation.TimeLapseGroup;

public class ParallelInterval<T extends TickListener<TimeInterval>> extends Parallel<T, TimeInterval> {

    public ParallelInterval(boolean register, Class<T> targetClass) {
        super(register, targetClass);
    }
    @Override
    protected TimeInterval getI(TimeInterval interval) {
        return interval;
    }
    
}
