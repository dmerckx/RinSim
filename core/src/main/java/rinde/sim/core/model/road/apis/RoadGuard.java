package rinde.sim.core.model.road.apis;

import rinde.sim.core.graph.Point;
import rinde.sim.core.model.SafeIterator;
import rinde.sim.core.model.road.RoadModel;
import rinde.sim.core.model.road.users.FixedRoadUser;
import rinde.sim.core.model.road.users.MovingRoadUser;
import rinde.sim.core.model.road.users.RoadData;
import rinde.sim.core.model.road.users.RoadUser;
import rinde.sim.util.positions.Query;

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
    public synchronized Point getCurrentLocation() {
        return lastLocation;
    }

    @Override
    public synchronized Point getLocation() {
        return lastLocation;
    }
    
    @Override
    public synchronized RoadState getState(){
        return this;
    }
    
    
    // ----- ROAD QUERIES ----- //

    @Override
    public synchronized <T extends RoadUser<?>> void queryAround(Point pos, double range, Query<T> q) {
        model.queryAround(pos, range, q);
    }

    @Override
    public synchronized SafeIterator<RoadUser<?>> queryRoadUsers() {
        return model.queryRoadUsers();
    }

    @Override
    public synchronized SafeIterator<FixedRoadUser<?>> queryFixedRoadUsers() {
        return model.queryFixedRoadUsers();
    }

    @Override
    public synchronized SafeIterator<MovingRoadUser<?>> queryMovingRoadUsers() {
        return model.queryMovingRoadUsers();
    }
}
