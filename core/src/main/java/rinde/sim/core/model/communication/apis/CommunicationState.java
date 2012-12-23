package rinde.sim.core.model.communication.apis;

import rinde.sim.core.model.communication.Address;
import rinde.sim.core.model.road.users.RoadUser;

/**
 * The communication state of a certain {@link RoadUser}.
 * This object is guaranteed to always return the same results 
 * during a single tick.
 * 
 * @author dmerckx
 */
public abstract class CommunicationState {

    CommunicationState() {}
    
    /**
     * Returns the unique address of this {@link RoadUser}.
     * @return The unique address.
     */
    public abstract Address getAddress();
}
