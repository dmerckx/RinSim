package rinde.sim.core.simulation;

public interface TimeInterval {

    /**
     * @return The start time of this time lapse.
     */
    public long getStartTime();

    /**
     * @return The end time of this time lapse.
     */
    public long getEndTime();
    
    
    public long getTimeStep();
}
