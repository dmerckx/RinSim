package rinde.sim.core.simulation.time;

import static com.google.common.base.Preconditions.checkArgument;
import rinde.sim.core.simulation.TimeInterval;

public class TimeIntervalImpl implements TimeInterval {

    protected long startTime;
    protected long endTime;
    
    public TimeIntervalImpl() {}

    public TimeIntervalImpl(long start, long end) {
        checkArgument(start >= 0, "time must be positive");
        checkArgument(end > start, "end time must be after start time");
        
        initialize(start, end);
    }
    
    public TimeInterval initialize(long start, long end) {
        startTime = start;
        endTime = end;
        return this;
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
    public String toString() {
        return new StringBuilder("[").append(startTime).append(",")
                .append(endTime).append(")").toString();
    }

}
