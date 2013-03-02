package rinde.sim.core.simulation.time;

import static com.google.common.base.Preconditions.checkArgument;
import rinde.sim.core.simulation.TimeInterval;

public class TimeIntervalImpl implements TimeInterval {

    private long time;
    private final long step;

    public TimeIntervalImpl(long start, long step) {
        checkArgument(start >= 0, "time must be positive");
        checkArgument(step > 0, "time must advance");
        
        this.time = start;
        this.step = step;
    }
    
    public void nextStep(){
        time += step;
    }
    
    @Override
    public long getStartTime() {
        return time;
    }
    
    @Override
    public long getEndTime() {
        return time + step;
    }

    @Override
    public long getTimeStep() {
        return step;
    }

    @Override
    public String toString() {
        return "[" + getStartTime() + "," + getEndTime() + "]";
    }
}
