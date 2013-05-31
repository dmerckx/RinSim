package rinde.sim.core.model.road.apis;

import rinde.sim.core.graph.Point;
import rinde.sim.core.model.road.RoadModel;
import rinde.sim.core.model.road.users.RoadData;
import rinde.sim.core.model.road.users.RoadUser;
import rinde.sim.core.simulation.time.TimeLapseHandle;
import rinde.sim.util.concurrency.FixedValueCache;
import rinde.sim.util.concurrency.ValueCache;
import rinde.sim.util.concurrency.VariableValueCache;
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
    protected final int id;
    protected final TimeLapseHandle handle;
    
    /**
     * Construct a new guard. 
     * @param user The user to which this API belongs.
     * @param data The initialization data for this API.
     * @param model The road model.
     */
    @SuppressWarnings("hiding")
    public RoadGuard(RoadUser<?> user, RoadData data, RoadModel model, TimeLapseHandle handle, int id, boolean fixed) {
        this.user = user;
        this.model = model;
        this.location = fixed? new FixedValueCache<Point>(data.getStartPosition()): new VariableValueCache<Point>(data.getStartPosition(), handle);
        this.id = id;
        this.handle = handle;
    }
    
    public TimeLapseHandle getHandle(){
        return handle;
    }
    
    // ----- ROAD API ----- //
    
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

    @Override
    public Integer getId() {
        return id;
    }
    
    
    // ----- ROAD QUERIES ----- //

    @Override
    public <T extends RoadUser<?>> void queryAround(Point pos, double range, Query<T> q) {
        model.queryAround(pos, range, q);
    }
}
