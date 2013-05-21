package rinde.sim.core.model.road.apis;

import rinde.sim.core.graph.Point;
import rinde.sim.core.model.road.RoadModel;
import rinde.sim.core.model.road.users.RoadData;
import rinde.sim.core.model.road.users.RoadUser;
import rinde.sim.core.simulation.time.TimeLapseHandle;
import rinde.sim.util.concurrency.ValueCache;
import rinde.sim.util.positions.Query;

/**
 * An implementation of the {@link RoadAPI}.
 * 
 * This guard guarantees location consistency:
 *  - the state of this API will never change during a single tick
 * 
 * @author dmerckx
 */
@SuppressWarnings("javadoc")
public class RoadGuard extends RoadState implements RoadAPI{
    protected final RoadModel model;
    protected final RoadUser<?> user;
    protected final ValueCache<Point> location;
    
    /**
     * Construct a new guard. 
     * @param user The user to which this API belongs.
     * @param data The initialization data for this API.
     * @param model The road model.
     */
    @SuppressWarnings("hiding")
    public RoadGuard(RoadUser<?> user, RoadData data, RoadModel model, TimeLapseHandle handle) {
        this.user = user;
        this.model = model;
        this.location = new ValueCache<Point>(data.getStartPosition(), handle);
    }
    
    @Override
    public Point getCurrentLocation() {
        return location.getActualValue();
    }

    @Override
    public Point getLocation() {
        return location.getFrozenValue();
    }
    
    @Override
    public RoadState getState(){
        return this;
    }
    
    
    // ----- ROAD QUERIES ----- //

    @Override
    public <T extends RoadUser<?>> void queryAround(Point pos, double range, Query<T> q) {
        model.queryAround(pos, range, q);
    }
}
