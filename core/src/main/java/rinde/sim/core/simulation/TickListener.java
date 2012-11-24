package rinde.sim.core.simulation;

/**
 * Allows an implementor to receive updates when time progresses in the
 * simulator.
 * 
 * @author Rinde van Lon (rinde.vanlon@cs.kuleuven.be)
 * @author Bartosz Michalik <bartosz.michalik@cs.kuleuven.be>
 * @author dmerckx
 * 
 * @param <T> The type of time this tick listener will receive.
 *      In the most general setup a listener will only require
 *      a {@link TimeInterval} to know about the current time progress.
 *      But this {@link TimeInterval} could be expanded to a more powerful
 *      type such as a {@link TimeLapse} which can be 'consumed' by the 
 *      {@link TickListener}.
 */
public interface TickListener<T extends TimeInterval> {
    
    /**
     * Is called when time has progressed a single 'tick' (time step). The
     * provided {@link TimeLapse} object provides information about the current
     * time.
     * 
     * Note:<b> a reference to the {@link TimeLapse} object should never be
     * kept</b>. The time lapse object will be consumed by default after the
     * this method is finished.
     * @param time The time that is handed to this object.
     */
    public void tick(TimeInterval time);
}
