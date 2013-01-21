package rinde.sim.core.simulation.time;

import static com.google.common.base.Preconditions.checkArgument;
import rinde.sim.core.simulation.Simulator;
import rinde.sim.core.simulation.TimeLapse;

public class TimeLapseHandle extends TimeIntervalImpl implements TimeLapse{
    
    private long step;
    private long schedualedUntil;
    private boolean blocked = false;
    
    public TimeLapseHandle(long start, long step) {
        super(start, start+step);
        this.step = step;
        this.schedualedUntil = start;
    }
    
    public boolean isBlocked(){
        return blocked;
    }
    
    public long getSchedualedUntil(){
        return schedualedUntil;
    }
    
    /**
     * Set the timelapse to the next interval.
     * Should only be called by the {@link Simulator} after an entire
     * tick has been handled.
     */
    public void nextStep(){
        
        startTime += step;
        endTime += step;
        
        if(schedualedUntil < startTime)
            schedualedUntil = startTime;
        
        if(blocked) 
            schedualedUntil = endTime;
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
        checkArgument(hasTimeLeft(),
                "there must be some amount of time left to perform a new action");
        schedualedUntil += time;
    }

    /**
     * Consumes the entire time lapse.
     */
    @Override
    public void consumeAll() {
        checkArgument(hasTimeLeft(),
                "there must be some amount of time left to perform a new action");
        schedualedUntil += getTimeLeft();
        
        assert schedualedUntil == getEndTime();
    }

    /**
     * @return If there is time left to consume.
     */
    @Override
    public boolean hasTimeLeft() {
        return getTimeLeft() > 0;
    }

    /**
     * @return The amount of time left to consume.
     */
    @Override
    public long getTimeLeft() {
        long timeLeft = getEndTime() - schedualedUntil;
        
        return timeLeft > 0 ? timeLeft : 0;
    }

    /**
     * @return The current time taking into account any time consumption in this
     *         instance. When looking for the time at the start of this time
     *         lapse use {@link #getStartTime()}.
     */
    @Override
    public long getCurrentTime() {
        return endTime - getTimeLeft();
    }

    /**
     * @return The step (or length) of this time lapse.
     */
    @Override
    public long getTimeStep() {
        return step;
    }

    /**
     * @return The time that was previously consumed in this time lapse.
     */
    @Override
    public long getTimeConsumed() {
        return step - getTimeLeft();
    }

    @Override
    public String toString() {
        return super.toString() + "{" + schedualedUntil +"}";
    }

    /**
     * Blocks this time lapse indefinitely. No consumable time is ever received
     * until this block wears off. 
     */
    public void block() {
        assert schedualedUntil <= endTime;
        schedualedUntil = endTime;
        
        blocked = true;
    }

    /**
     * Unblocks this time lapse. An additional extra amount of time can be consumed
     * before time is available again. 
     * @param extraTime The extra time to add to the consumption.
     */
    public void unblock(long extraTime){
        assert blocked && schedualedUntil == endTime;
        
        blocked = false;
        schedualedUntil = endTime + extraTime;
    }
}
