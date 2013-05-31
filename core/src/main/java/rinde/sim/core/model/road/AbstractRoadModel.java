/**
 * 
 */
package rinde.sim.core.model.road;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;

import rinde.sim.core.graph.Point;
import rinde.sim.core.model.road.apis.MovingRoadGuard;
import rinde.sim.core.model.road.apis.RoadGuard;
import rinde.sim.core.model.road.users.FixedRoadUser;
import rinde.sim.core.model.road.users.MovingRoadData;
import rinde.sim.core.model.road.users.MovingRoadUser;
import rinde.sim.core.model.road.users.RoadData;
import rinde.sim.core.model.road.users.RoadUser;
import rinde.sim.core.simulation.TimeInterval;
import rinde.sim.core.simulation.TimeLapse;
import rinde.sim.core.simulation.UserInit;
import rinde.sim.core.simulation.policies.InteractionRules;
import rinde.sim.core.simulation.time.TimeLapseHandle;
import rinde.sim.util.SpeedConverter;
import rinde.sim.util.TimeUnit;
import rinde.sim.util.positions.ConcurrentPositionCache;
import rinde.sim.util.positions.Query;

import com.google.common.collect.Lists;

/**
 * A common space neutral implementation of {@link RoadModel}. It implements a
 * data structure for managing objects and locations and checks many
 * preconditions as defined in {@link RoadModel}.
 * @param <T> The type of the location representation that is used for storing
 *            object locations. This location representation should only be used
 *            internally in the model.
 * 
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 */
public abstract class AbstractRoadModel<T> implements RoadModel{
    public final AtomicInteger queries = new AtomicInteger();

    private int idCounter = 0;
    private RandomGenerator rnd;
    private Map<RoadUser<?>, RoadGuard> mapping = new HashMap<RoadUser<?>, RoadGuard>();
    protected Map<RoadUser<?>, T> objLocs;
    public static ConcurrentPositionCache<RoadUser<?>> cache;
    
    //Cache variables
    private boolean cached;
    private int blocks;
    
    protected final SpeedConverter speedConverter;
    protected final boolean useSpeedConversion;

    /**
     * Create a new instance.
     */
    public AbstractRoadModel(boolean pUseSpeedConversion) {
        objLocs = new LinkedHashMap<RoadUser<?>, T>();
        speedConverter = new SpeedConverter();
        useSpeedConversion = pUseSpeedConversion;
        
        cached = false;
        cache = null;
    }
    
    public void setCache(int blocks){
        if(blocks <= 0) throw new IllegalArgumentException();
        
        this.cached = true;
        this.blocks = blocks;
    }
    
    public double getSpeed(MovingRoadUser<?> user){
        return ((MovingRoadGuard) mapping.get(user)).getSpeed();
    }

    @Override
    public <T2 extends RoadUser<?>> void queryAround(Point pos, double range, Query<T2> query){
        //TODO: queries.incrementAndGet();
        
        if(cached){
            //throw new IllegalStateException();
            cache.query(pos, range, query);
        }
        else {
            //Simply iteratie over all road users
            for(RoadUser<?> obj:mapping.keySet()){
                if(!query.getType().isInstance(obj)) continue;
                if(Point.distance(obj.getRoadState().getLocation(), pos) < range){
                    query.process((T2) obj);
                } 
            }
        }
    }


    @Override
    public Class<RoadUser<?>> getSupportedType() {
        return (Class) RoadUser.class;
    }

    /**
     * A function for converting the location representation to a {@link Point}.
     * @param locObj The location to be converted.
     * @return A {@link Point} indicating the position as represented by the
     *         specified location.
     */
    protected abstract Point locObj2point(T locObj);

    /**
     * A function for converting a {@link Point} to the location representation
     * of this model.
     * @param point The {@link Point} to be converted.
     * @return The location.
     */
    protected abstract T point2LocObj(Point point);

    /**
     * This method should convert speed values to the unit that is used in the
     * model. E.g. if speed is defined as km/hour, but the model uses TODO
     * refine doc
     * @param speed
     * @return
     */
    protected double speedToSpaceUnit(double speed) {
        if (useSpeedConversion) {
            // speed in graph units per hour -> converting to milliseconds
            return speedConverter.from(speed, TimeUnit.H).to(TimeUnit.MS);
        } else {
            return speed;
        }
    }

    @Override
    public final MoveProgress followPath(MovingRoadUser<?> object,
            Queue<Point> path, TimeLapse time) {
        checkArgument(objLocs.containsKey(object), "object must have a location");
        checkArgument(path.peek() != null, "path can not be empty");
        checkArgument(time.hasTimeLeft(), "can not follow path when to time is left");
        final MoveProgress mp = doFollowPath(object, path, time);
        return mp;
    }

    /**
     * Should be overriden by subclasses to define actual
     * {@link RoadModel#followPath(MovingRoadUser<?>, Queue, TimeLapse)} behavior.
     * @param object The object that is moved.
     * @param path The path that is followed.
     * @param time The time that is available for travel.
     * @return A {@link MoveProgress} instance containing the actual travel
     *         details.
     */
    protected abstract MoveProgress doFollowPath(MovingRoadUser<?> object,
            Queue<Point> path, TimeLapse time);

    protected void addObjectAt(RoadUser<?> newObj, Point pos) {
        checkArgument(!objLocs.containsKey(newObj), "Object is already added");
        objLocs.put(newObj, point2LocObj(pos));
        if(cached) cache.add(newObj);
    }
    
    protected void updateObject(RoadUser<?> obj, Point to){
        checkArgument(objLocs.containsKey(obj), "Object was not yet present");
        if(cached) cache.update(obj, getPosition(obj), to);
        objLocs.put(obj, point2LocObj(to));
    }

    protected void removeObject(RoadUser<?> roadUser) {
        checkArgument(roadUser != null, "RoadUser<?> can not be null");
        checkArgument(objLocs.containsKey(roadUser), "RoadUser<?>: %s  does not exist.", roadUser);
        objLocs.remove(roadUser);
        if(cached) cache.remove(roadUser);
    }
    
    @Override
    public Point getPosition(RoadUser<?> roadUser) {
        checkArgument(roadUser != null, "object can not be null");
        checkArgument(objLocs.containsKey(roadUser), "RoadUser<?> does not exist");
        return locObj2point(objLocs.get(roadUser));
    }

    @Override
    public Set<RoadUser<?>> getAllRoadUsers() {
        synchronized (objLocs) {
            final Set<RoadUser<?>> copy = new LinkedHashSet<RoadUser<?>>();
            copy.addAll(objLocs.keySet());
            return copy;
        }
    }

    @Override
    public List<Point> getShortestPathTo(RoadUser<?> fromObj, Point to) {
        checkArgument(fromObj != null, "fromObj can not be null");
        checkArgument(objLocs.containsKey(fromObj), " from object should be in RoadModel. "
                + fromObj);
        return getShortestPathTo(getPosition(fromObj), to);
    }
    
    // ----- MODEL IMPLEMENTATION ----- //

    @Override
    public List<UserInit<?>> register(RoadUser<?> user, RoadData data, TimeLapseHandle handle) {
        assert user!=null : "User can not be null.";
        assert data!=null : "Data can not be null.";
        
        if( user instanceof MovingRoadUser<?>){
            assert data instanceof MovingRoadData: "Data must fit user";
            
            MovingRoadGuard guard = new MovingRoadGuard((MovingRoadUser<?>) user, (MovingRoadData) data, this, rnd.nextLong(), handle, idCounter++);
            ((MovingRoadUser<?>) user).setRoadAPI(guard);
            mapping.put(user, guard);
        }
        else if( user instanceof FixedRoadUser<?>){
            
            RoadGuard guard = new RoadGuard(user, data, this, handle, idCounter++, true);
            ((FixedRoadUser<?>) user).setRoadAPI(guard);
            mapping.put(user, guard);
        }
        else {
            throw new IllegalArgumentException("The user " + user + " has not a valid type, known by this road model");
        }
        addObjectAt(user, data.getStartPosition());
        
        return Lists.newArrayList();
    }

    @Override
    public void unregister(RoadUser<?> user) {
        assert user!=null : "User can not be null.";
        assert objLocs.containsKey(user) : "The user has to be present in this model";
        
        removeObject(user);
        mapping.remove(user);
    }

    @Override
    public void tick(TimeInterval time) {
        //if(cached) cache.tick();
    }

    @Override
    public void init(long seed, InteractionRules rules, TimeInterval masterTime) {
        this.rnd = new MersenneTwister(seed);
        this.cache = new ConcurrentPositionCache(getViewRect(), blocks, masterTime);
    }
}

class MoveProgress {
    /**
     * Distance traveled in the
     * {@link RoadModel#followPath(MovingRoadUser, java.util.Queue, rinde.sim.core.TimeLapse)}
     * .
     */
    public final double distance;
    /**
     * Time spend on traveling the distance.
     */
    public final long time;

    /**
     * The nodes which were traveled.
     */
    public final List<Point> travelledNodes;

    MoveProgress(double dist, long pTime, List<Point> pTravelledNodes) {
        checkArgument(dist >= 0, "distance must be greater than or equal to 0");
        checkArgument(pTime >= 0, "time must be greather than or equal to 0");
        checkArgument(pTravelledNodes != null, "travelledNodes can not be null");
        distance = dist;
        time = pTime;
        travelledNodes = pTravelledNodes;
    }

    @Override
    public String toString() {
        return new StringBuilder().append("{PathProgress distance:")
                .append(distance).append(" time:").append(time)
                .append(" travelledNodes:").append(travelledNodes).append("}")
                .toString();
    }
}
