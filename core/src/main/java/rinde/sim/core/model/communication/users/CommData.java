package rinde.sim.core.model.communication.users;

import rinde.sim.core.model.road.users.RoadData;

/**
 * Initialization data for {@link CommUser}s.
 * 
 * @author dmerckx
 */
public interface CommData extends SimpleCommData, RoadData {
    /**
     * The initial radius to be used for broadcasting messages.
     * @return The initial radius.
     */
    Double getInitialRadius();
}
