package rinde.sim.core.simulation;

import static com.google.common.base.Preconditions.checkArgument;

public class TimeInterval {

    protected long startTime;
    protected long endTime;
    
    TimeInterval() {}

    TimeInterval(long start, long end) {
        initialize(start, end);
    }
    
    TimeInterval initialize(long start, long end) {
        checkArgument(start >= 0, "time must be positive");
        checkArgument(end > start, "end time must be after start time");
        startTime = start;
        endTime = end;
        return this;
    }

    /**
     * @return The start time of this time lapse.
     */
    public long getStartTime() {
        return startTime;
    }

    /**
     * @return The end time of this time lapse.
     */
    public long getEndTime() {
        return endTime;
    }

    @Override
    public String toString() {
        return new StringBuilder("[").append(startTime).append(",")
                .append(endTime).append(")").toString();
    }

}
