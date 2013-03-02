package rinde.sim.core.simulation.time;

import static com.google.common.base.Preconditions.checkArgument;
import rinde.sim.core.simulation.TimeInterval;
import rinde.sim.core.simulation.TimeLapse;

public class TimeLapseHandle implements TimeLapse{
    
    private final TimeInterval masterTime;
    
    private long schedualedUntil;
    private boolean blocked = false;
    
    
    public TimeLapseHandle(TimeInterval masterTime) {
        this.masterTime = masterTime;
    }
    
    public boolean isBlocked(){
        return blocked;
    }
    
    public long getSchedualedUntil(){
        return schedualedUntil < getStartTime()? getStartTime():schedualedUntil;
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
        
        schedualedUntil = schedualedUntil < getStartTime()?
                    getStartTime() + time :
                    schedualedUntil + time;
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
        long timeLeft = getEndTime() - getSchedualedUntil();
        
        return timeLeft > 0 ? timeLeft : 0;
    }

    /**
     * @return The current time taking into account any time consumption in this
     *         instance. When looking for the time at the start of this time
     *         lapse use {@link #getStartTime()}.
     */
    @Override
    public long getCurrentTime() {
        return getEndTime() - getTimeLeft();
    }

    /**
     * @return The time that was previously consumed in this time lapse.
     */
    @Override
    public long getTimeConsumed() {
        return getTimeStep() - getTimeLeft();
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
        schedualedUntil = Long.MAX_VALUE;
        
        blocked = true;
    }

    /**
     * Unblocks this time lapse. 
     */
    public void unblock(){
        unblock(getStartTime());
    }

    /**
     * Unblocks this time lapse. An additional extra amount of time can be consumed
     * before time is available again. 
     * @param extraTime The extra time to add to the consumption.
     */
    public void unblock(long unblockTime){
        assert blocked && schedualedUntil == Long.MAX_VALUE;
        assert unblockTime >= getStartTime();
        
        schedualedUntil = unblockTime;
        blocked = false;
    }
    
    //-----TIME INTERVAL-----//

    @Override
    public long getStartTime() {
        return masterTime.getStartTime();
    }

    @Override
    public long getEndTime() {
        return masterTime.getEndTime();
    }
    
    @Override
    public long getTimeStep() {
        return masterTime.getTimeStep();
    }
}
