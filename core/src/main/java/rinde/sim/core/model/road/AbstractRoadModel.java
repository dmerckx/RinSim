/**
 * 
 */
package rinde.sim.core.model.road;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;

import rinde.sim.core.graph.Point;
import rinde.sim.core.model.SafeIterator;
import rinde.sim.core.model.User;
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
import rinde.sim.util.positions.PositionCache;
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

    private RandomGenerator rnd;
    private Map<RoadUser<?>, RoadGuard> mapping = new HashMap<RoadUser<?>, RoadGuard>();
    protected volatile Map<RoadUser<?>, T> objLocs;

    private boolean cached;
    private PositionCache<RoadUser<?>> cache;
    
    protected final SpeedConverter speedConverter;
    protected final boolean useSpeedConversion;

    /**
     * Create a new instance.
     */
    public AbstractRoadModel(boolean pUseSpeedConversion) {
        objLocs = createObjectToLocationMap();
        speedConverter = new SpeedConverter();
        useSpeedConversion = pUseSpeedConversion;
        
        cached = false;
        cache = null;
    }
    
    public void setCache(int blocks){
        if(blocks <= 0) throw new IllegalArgumentException();
        
        cached = true;
        cache = new PositionCache<RoadUser<?>>(getViewRect(), blocks);
    }
    
    public double getSpeed(MovingRoadUser<?> user){
        return ((MovingRoadGuard) mapping.get(user)).getSpeed();
    }

    @Override
    public <T2 extends RoadUser<?>> void queryAround(Point pos, double range, Query<T2> query){
        if(cached){
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
    public SafeIterator<RoadUser<?>> queryRoadUsers() {
        return new SafeIterator.Std<RoadUser<?>>(mapping.keySet());
    }

    @Override
    public SafeIterator<FixedRoadUser<?>> queryFixedRoadUsers() {
        final Iterator<RoadUser<?>> it1 = mapping.keySet().iterator();
        final Iterator<RoadUser<?>> it2 = mapping.keySet().iterator();
        return new SafeIterator<FixedRoadUser<?>>(){
            @Override
            public boolean hasNext() {
                while(it1.hasNext()){
                    if(it1.next() instanceof FixedRoadUser<?>) return true;
                }
                return false;
            }
            @Override
            public FixedRoadUser<?> next() {
                RoadUser<?> result = it2.next();
                while(!(result instanceof FixedRoadUser<?>)){
                    result = it2.next();
                }
                return (FixedRoadUser<?>) result;
            }
        };
    }
    
    @Override
    public SafeIterator<MovingRoadUser<?>> queryMovingRoadUsers() {
        final Iterator<RoadUser<?>> it1 = mapping.keySet().iterator();
        final Iterator<RoadUser<?>> it2 = mapping.keySet().iterator();
        return new SafeIterator<MovingRoadUser<?>>(){
            @Override
            public boolean hasNext() {
                while(it1.hasNext()){
                    if(it1.next() instanceof MovingRoadUser<?>) return true;
                }
                return false;
            }
            @Override
            public MovingRoadUser<?> next() {
                RoadUser<?> result = it2.next();
                while(!(result instanceof MovingRoadUser<?>)){
                    result = it2.next();
                }
                return (MovingRoadUser<?>) result;
            }
        };
    }

    @Override
    public Class<RoadUser<?>> getSupportedType() {
        return (Class) RoadUser.class;
    }

    /**
     * Defines the specific {@link Map} instance used for storing object
     * locations.
     * @return The map instance.
     */
    protected Map<RoadUser<?>, T> createObjectToLocationMap() {
        return Collections.synchronizedMap(new LinkedHashMap<RoadUser<?>, T>());
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

    protected boolean containsObject(RoadUser<?> obj) {
        checkArgument(obj != null, "obj can not be null");
        return objLocs.containsKey(obj);
    }
    
    @Override
    public Point getPosition(RoadUser<?> roadUser) {
        checkArgument(roadUser != null, "object can not be null");
        checkArgument(containsObject(roadUser), "RoadUser<?> does not exist");
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
            
            MovingRoadGuard guard = new MovingRoadGuard((MovingRoadUser<?>) user, (MovingRoadData) data, this, rnd.nextLong(), handle);
            ((MovingRoadUser<?>) user).setRoadAPI(guard);
            mapping.put(user, guard);
        }
        else if( user instanceof FixedRoadUser<?>){
            
            RoadGuard guard = new RoadGuard(user, data, this, handle);
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
        assert containsObject(user) : "The user has to be present in this model";
        
        removeObject(user);
        mapping.remove(user);
    }

    @Override
    public void tick(TimeInterval time) {
        if(cached) cache.tick();
    }
    
    @Override
    public void init(long seed, InteractionRules rules, TimeInterval masterTime) {
        this.rnd = new MersenneTwister(seed);
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
