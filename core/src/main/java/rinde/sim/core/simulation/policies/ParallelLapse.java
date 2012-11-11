package rinde.sim.core.simulation.policies;

import rinde.sim.core.simulation.TickListener;
import rinde.sim.core.simulation.TimeInterval;
import rinde.sim.core.simulation.TimeLapse;
import rinde.sim.core.simulation.time.TimeLapseGroup;

public class ParallelLapse<T extends TickListener<TimeLapse>> extends Parallel<T, TimeLapse> {

    private TimeLapseGroup group = new TimeLapseGroup();
    
    public ParallelLapse(boolean register, Class<T> targetClass) {
        super(register, targetClass);
    }
    @Override
    protected TimeLapse getI(TimeInterval interval) {
        return group.forge(interval);
    }
    
}
