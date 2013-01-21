package rinde.sim.core.simulation.time;

import static com.google.common.base.Preconditions.checkArgument;
import rinde.sim.core.simulation.TimeInterval;

public class TimeIntervalImpl implements TimeInterval {

    protected long startTime;
    protected long endTime;

    public TimeIntervalImpl(long start, long end) {
        checkArgument(start >= 0, "time must be positive");
        checkArgument(end > start, "end time must be after start time");
        
        this.startTime = start;
        this.endTime = end;
    }
    
    @Override
    public long getStartTime() {
        return startTime;
    }
    
    @Override
    public long getEndTime() {
        return endTime;
    }

    @Override
    public long getTimeStep() {
        return startTime - endTime;
    }

    @Override
    public String toString() {
        return /*super.toString() +*/ "[" + startTime + "," + endTime + "]";
    }
}
