package rinde.sim.core.model.road.apis;

import rinde.sim.core.graph.Point;
import rinde.sim.core.model.road.RoadModel;
import rinde.sim.core.model.road.users.RoadData;
import rinde.sim.core.model.road.users.RoadUser;

/**
 * An implementation of the {@link RoadAPI}.
 * 
 * This guard guarantees location consistency:
 *  - the state of this API will never change during a single tick
 * 
 * @author dmerckx
 */
public class RoadGuard extends RoadState implements RoadAPI{

    @SuppressWarnings("javadoc")
    protected RoadModel model;
    @SuppressWarnings("javadoc")
    protected RoadUser<?> user;
    @SuppressWarnings("javadoc")
    protected Point lastLocation;
    
    /**
     * Construct a new guard. 
     * @param user The user to which this API belongs.
     * @param data The initialization data for this API.
     * @param model The road model.
     */
    @SuppressWarnings("hiding")
    public RoadGuard(RoadUser<?> user, RoadData data, RoadModel model) {
        this.user = user;
        this.model = model;
        this.lastLocation = data.getStartPosition();
    }
    
    @Override
    public Point getCurrentLocation() {
        return lastLocation;
    }

    @Override
    public Point getLocation() {
        return lastLocation;
    }
    
    @Override
    public RoadState getState(){
        return this;
    }

}
