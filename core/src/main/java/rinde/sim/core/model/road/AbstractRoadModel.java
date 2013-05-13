/**
 * 
 */
package rinde.sim.core.model.road;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Maps.newLinkedHashMap;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
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
import rinde.sim.event.EventAPI;
import rinde.sim.event.EventDispatcher;
import rinde.sim.util.SpeedConverter;
import rinde.sim.util.TimeUnit;
import rinde.sim.util.positions.Query;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

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
    protected Map<MovingRoadUser<?>, DestinationPath> objDestinations;

    protected final boolean cached;
    protected final SpeedConverter speedConverter;

    protected boolean useSpeedConversion;

    // TODO event dispatching has to be tested
    protected final EventDispatcher eventDispatcher;
    protected final EventAPI eventAPI;

    public enum RoadEvent {
        MOVE
    }
    
    public double getSpeed(MovingRoadUser<?> user){
        return ((MovingRoadGuard) mapping.get(user)).getSpeed();
    }
    
    @Override
    public <T2 extends RoadUser<?>> void queryAround(Point pos, double range, Query<T2> q){
        for(T obj:map.get(reg)){
            if(!q.getType().isInstance(obj)) return;
            
            if(Point.distance(obj.getRoadState().getLocation(), pos) < range){
                q.process((T2) obj);
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
    
    public AbstractRoadModel(boolean pUseSpeedConversion){
        this(pUseSpeedConversion, 0);
    }
    
    /**
     * Create a new instance.
     */
    public AbstractRoadModel(boolean pUseSpeedConversion, int blocks) {
        objLocs = createObjectToLocationMap();
        objDestinations = newLinkedHashMap();
        speedConverter = new SpeedConverter();
        useSpeedConversion = pUseSpeedConversion;
        eventDispatcher = createEventDispatcher();
        eventAPI = eventDispatcher.getEventAPI();
        
        cached = true;
    }

    // factory method for creating event dispatcher, can be overridden by
    // subclasses to add more event types.
    protected EventDispatcher createEventDispatcher() {
        return new EventDispatcher(RoadEvent.MOVE);
    }

    /**
     * Create a new instance.
     */
    public AbstractRoadModel() {
        this(true);
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
        objDestinations.remove(object);
        final MoveProgress mp = doFollowPath(object, path, time);
        eventDispatcher.dispatchEvent(new MoveEvent(this, object, mp));
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
    }

    protected void addObjectAtSamePosition(RoadUser<?> newObj, RoadUser<?> existingObj) {
        checkArgument(!objLocs.containsKey(newObj), "Object " + newObj
                + " is already added.");
        checkArgument(objLocs.containsKey(existingObj), "Object " + existingObj
                + " does not exist.");
        objLocs.put(newObj, objLocs.get(existingObj));
    }

    protected void removeObject(RoadUser<?> roadUser) {
        checkArgument(roadUser != null, "RoadUser<?> can not be null");
        checkArgument(objLocs.containsKey(roadUser), "RoadUser<?>: " + roadUser
                + " does not exist.");
        objLocs.remove(roadUser);
        objDestinations.remove(roadUser);
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
    public Set<RoadUser<?>> getRoadUsers(Predicate<RoadUser<?>> predicate) {
        return Sets.filter(getAllRoadUsers(), predicate);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <Y extends RoadUser<?>> Set<Y> getObjectsAt(Point location, Class<Y> type) {
        checkArgument(type != null, "type can not be null");
        final Set<Y> result = new HashSet<Y>();
        for (final RoadUser<?> ru :
                getRoadUsers(new SameLocationPredicate(location,type, this))) {
            result.add((Y) ru);
        }
        return result;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <Y extends RoadUser<?>> Set<Y> getObjectsOfType(final Class<Y> type) {
        if (type == null) {
            throw new IllegalArgumentException("type can not be null");
        }
        return (Set<Y>) getRoadUsers(new Predicate<RoadUser<?>>() {
            @Override
            public boolean apply(RoadUser<?> input) {
                return type.isInstance(input);
            }
        });
    }

    @Override
    public List<Point> getShortestPathTo(RoadUser<?> fromObj, Point to) {
        checkArgument(fromObj != null, "fromObj can not be null");
        checkArgument(objLocs.containsKey(fromObj), " from object should be in RoadModel. "
                + fromObj);
        return getShortestPathTo(getPosition(fromObj), to);
    }

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
            
            RoadGuard guard = new RoadGuard(user, data, this);
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
    public List<User<?>> unregister(RoadUser<?> user) {
        assert user!=null : "User can not be null.";
        assert containsObject(user) : "The user has to be present in this model";
        
        removeObject(user);
        mapping.remove(user);
        
        return Lists.newArrayList();
    }

    @Override
    public final EventAPI getEventAPI() {
        return eventAPI;
    }

    private static class SameLocationPredicate implements Predicate<RoadUser<?>> {
        private final Point location;
        private final RoadModel model;
        private final Class<?> type;

        public SameLocationPredicate(final Point pLocation,
                final Class<?> pType, final RoadModel pModel) {
            location = pLocation;
            type = pType;
            model = pModel;
        }

        @Override
        public boolean apply(RoadUser<?> input) {
            return type.isInstance(input)
                    && model.getPosition(input).equals(location);
        }
    }

    /**
     * Simple class for storing destinations and paths leading to them.
     * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
     */
    protected class DestinationPath {
        /**
         * The destination of the path.
         */
        public final Point destination;
        /**
         * The path leading to the destination.
         */
        public final Queue<Point> path;

        /**
         * Initializes a new instance.
         * @param dest {@link #destination}
         * @param p {@link #path}
         */
        public DestinationPath(Point dest, Queue<Point> p) {
            destination = dest;
            path = p;
        }
    }

    @Override
    public void tick(TimeInterval time) {
        
    }
    
    @Override
    public void init(long seed, InteractionRules rules, TimeInterval masterTime) {
        this.rnd = new MersenneTwister(seed);
    }
}
