package rinde.sim.core.model.interaction;

import rinde.sim.core.graph.Point;
import rinde.sim.core.model.interaction.apis.InteractionGuard;
import rinde.sim.core.model.interaction.users.InteractionUser;

/**
 * A receiver advertised by an {@link InteractionUser}.
 * This receiver will await {@link Visitor}s to interact with him.
 * {@link Visitor}s visiting the location of this receiver will have
 * access to all the public methods supplied by this receiver.
 * 
 * It is guaranteed that no 2 {@link Visitor}s will ever access any of
 * the public methods of this receiver concurrently.
 * 
 * After an interaction with a {@link Visitor} the receiver can change
 * state and possible terminate itself.
 *  
 * @author dmerckx
 */
@SuppressWarnings("hiding")
public class Receiver implements Comparable<Receiver>{
    
    /**
     * The location at which this receiver is stationed.
     */
    public final Point location;
    InteractionModel model;
    InteractionGuard guard;
    
    /**
     * Indicates whether this receiver is scheduled for termination.
     */
    protected boolean terminated = false;
    
    /**
     * Create a new receiver, which will be active at the given location.
     * @param location The location of the receiver.
     */
    public Receiver(Point location) {
        this.location = location;
    }
    
    /**
     * Terminate this receiver, the time lapse of the user will no longer be blocked.
     */
    public final void terminate(){
        terminate(0);
    }
    
    /**
     * Terminate this receiver, the time lapse of the user will no longer be blocked
     * after the given additional time cost has expired.
     * @param timeCost Additional time cost.
     */
    public final void terminate(long timeCost){
        assert !terminated: "Receiver can not be terminated more then ones";
        
        model.terminate(this, timeCost);
        terminated = true;
    }

    @Override
    public final int compareTo(Receiver o) {
        return guard.compareTo(o.guard);
    }
}
