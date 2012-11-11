/**
 * 
 */
package rinde.sim.core.simulation;


/**
 * Allows an implementor to receive updates when time progresses in the
 * simulator.
 * 
 * @author Rinde van Lon (rinde.vanlon@cs.kuleuven.be)
 * @author Bartosz Michalik <bartosz.michalik@cs.kuleuven.be>
 */
public interface Agent extends TickListener{

    /**
     * Is called when time has progressed a single 'tick' (time step). The
     * provided {@link TimeLapse} object provides information about the current
     * time. Further, an implementor can 'use' the provided time to perform
     * actions. Actions are methods that specify an operation (usually on a
     * model) that takes time. The {@link TimeLapse} reference that is received
     * throug this method can be used to spent on these time consuming actions. <br/>
     * <br/>
     * Note:<b> a reference to the {@link TimeLapse} object should never be
     * kept</b>. The time lapse object will be consumed by default after the
     * this method is finished.
     * @param timeLapse The time lapse that is handed to this object.
     */
    void tick(final TimeLapse timeLapse);
}
