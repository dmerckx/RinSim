package rinde.sim.core.simulation.time;

import static com.google.common.base.Preconditions.checkArgument;
import rinde.sim.core.simulation.TimeLapse;

public class TimeLapseImpl extends TimeIntervalImpl implements TimeLapse{
    
    private long timeLeft;

    public TimeLapseImpl() {}

    public TimeLapseImpl(long start, long end) {
        super(start, end);
    }
    
    @Override
    public TimeLapse initialize(long start, long end) {
        super.initialize(start, end);
        timeLeft = end - start;
        return this;
    }

    /**
     * Consumes the specified amount of time, where time must be strictly
     * positive and there must be enough time left as specified by
     * {@link #getTimeLeft()}.
     * @param time The time to consume.
     */
    @Override
    public void consume(long time) {
        checkArgument(time >= 0, "the time to consume must be a positive value");
        checkArgument(timeLeft - time >= 0, "there is not enough time left to consume "
                + time);
        timeLeft -= time;
    }

    /**
     * Consumes the entire time lapse.
     */
    @Override
    public void consumeAll() {
        timeLeft = 0;
    }

    /**
     * @return If there is time left to consume.
     */
    @Override
    public boolean hasTimeLeft() {
        return timeLeft > 0;
    }

    /**
     * @return The amount of time left to consume.
     */
    @Override
    public long getTimeLeft() {
        return timeLeft;
    }

    /**
     * @return The current time taking into account any time consumption in this
     *         instance. When looking for the time at the start of this time
     *         lapse use {@link #getStartTime()}.
     */
    @Override
    public long getTime() {
        return endTime - timeLeft;
    }

    /**
     * @return The step (or length) of this time lapse.
     */
    @Override
    public long getTimeStep() {
        return endTime - startTime;
    }

    /**
     * @return The time that was previously consumed in this time lapse.
     */
    @Override
    public long getTimeConsumed() {
        return (endTime - startTime) - timeLeft;
    }

    @Override
    public String toString() {
        return new StringBuilder("[").append(startTime).append(",")
                .append(endTime).append(")").toString();
    }

}
