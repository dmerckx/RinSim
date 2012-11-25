package rinde.sim.core.simulation;

import rinde.sim.core.model.Agent;


/**
 * Represents a consumable interval of time: [start, end). Instances of time
 * lapse are handed out by the {@link Simulator} and can be received by
 * implementing the {@link Agent} interface.
 * 
 * @author Rinde van Lon (rinde.vanlon@cs.kuleuven.be)
 */
public interface TimeLapse extends TimeInterval{


    /**
     * Consumes the specified amount of time, where time must be strictly
     * positive and there must be enough time left as specified by
     * {@link #getTimeLeft()}.
     * @param time The time to consume.
     */
    public void consume(long time);

    /**
     * Consumes the entire time lapse.
     */
    public void consumeAll();

    /**
     * @return If there is time left to consume.
     */
    public boolean hasTimeLeft();

    /**
     * @return The amount of time left to consume.
     */
    public long getTimeLeft();

    /**
     * @return The current time taking into account any time consumption in this
     *         instance. When looking for the time at the start of this time
     *         lapse use {@link #getStartTime()}.
     */
    public long getCurrentTime() ;

    /**
     * @return The step (or length) of this time lapse.
     */
    public long getTimeStep();

    /**
     * @return The time that was previously consumed in this time lapse.
     */
    public long getTimeConsumed();
}
