package rinde.sim.core.model.interaction;

import rinde.sim.core.graph.Point;
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
public class Receiver{
    
    /**
     * The location at which this receiver is stationed.
     */
    public final Point location;
    private InteractionModel model;
    
    protected boolean terminated = false;
    
    /**
     * Create a new receiver, which will be active at the given location.
     * @param location The location of the receiver.
     */
    @SuppressWarnings("hiding")
    public Receiver(Point location) {
        this.location = location;
    }
    
    /**
     * Set the interaction model, this model is in no way accessible by subclasses.
     * @param model The interaction model.
     */
    @SuppressWarnings("hiding")
    public final void setModel(InteractionModel model){
        this.model = model;
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
        model.terminate(this, timeCost);
        terminated = true;
    }
}
