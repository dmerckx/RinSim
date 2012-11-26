/**
 * 
 */
package rinde.sim.core.model.road;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Maps.newLinkedHashMap;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import rinde.sim.core.graph.Point;
import rinde.sim.core.model.road.guards.MovingRoadGuard;
import rinde.sim.core.model.road.guards.RoadGuard;
import rinde.sim.core.model.road.supported.MovingRoadUnit;
import rinde.sim.core.model.road.supported.RoadUnit;
import rinde.sim.core.model.road.users.MovingRoadUser;
import rinde.sim.core.model.road.users.RoadUser;
import rinde.sim.core.simulation.TimeInterval;
import rinde.sim.core.simulation.TimeLapse;
import rinde.sim.event.EventAPI;
import rinde.sim.event.EventDispatcher;
import rinde.sim.util.SpeedConverter;
import rinde.sim.util.TimeUnit;

import com.google.common.base.Predicate;
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

    private Map<RoadUser, RoadGuard> mapping = new HashMap<RoadUser, RoadGuard>();
    
    protected final SpeedConverter speedConverter;

    protected boolean useSpeedConversion;

    // TODO event dispatching has to be tested
    protected final EventDispatcher eventDispatcher;
    protected final EventAPI eventAPI;

    public enum RoadEvent {
        MOVE
    }
    
    public double getSpeed(MovingRoadUser user){
        return ((MovingRoadGuard) mapping.get(user)).getSpeed();
    }

    /**
     * A mapping of {@link RoadUser} to location.
     */
    protected volatile Map<RoadUser, T> objLocs;

    /**
     * A mapping of {@link MovingRoadUser}s to {@link DestinationPath}s.
     */
    protected Map<MovingRoadUser, DestinationPath> objDestinations;

    @Override
    public Class<RoadUnit> getSupportedType() {
        return RoadUnit.class;
    }
    
    /**
     * Create a new instance.
     */
    public AbstractRoadModel(boolean pUseSpeedConversion) {
        objLocs = createObjectToLocationMap();
        objDestinations = newLinkedHashMap();
        speedConverter = new SpeedConverter();
        useSpeedConversion = pUseSpeedConversion;
        eventDispatcher = createEventDispatcher();
        eventAPI = eventDispatcher.getEventAPI();
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
    protected Map<RoadUser, T> createObjectToLocationMap() {
        return Collections.synchronizedMap(new LinkedHashMap<RoadUser, T>());
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
    public final MoveProgress followPath(MovingRoadUser object,
            Queue<Point> path, TimeLapse time) {
        checkArgument(objLocs.containsKey(object), "object must have a location");
        checkArgument(path.peek() != null, "path can not be empty");
        checkArgument(time.hasTimeLeft(), "can not follow path when to time is left");
        objDestinations.remove(object);
        final MoveProgress mp = doFollowPath(object, path, time);
        eventDispatcher.dispatchEvent(new MoveEvent(this, object, mp));
        return mp;
    }

    @Override
    public MoveProgress moveTo(MovingRoadUser object, Point destination,
            TimeLapse time) {
        Queue<Point> path;
        if (objDestinations.containsKey(object)
                && objDestinations.get(object).destination.equals(destination)) {
            // is valid move? -> assume it is
            path = objDestinations.get(object).path;
        } else {
            path = new LinkedList<Point>(getShortestPathTo(object, destination));
            objDestinations.put(object, new DestinationPath(destination, path));
        }
        final MoveProgress mp = doFollowPath(object, path, time);
        eventDispatcher.dispatchEvent(new MoveEvent(this, object, mp));
        return mp;
    }

    @Override
    public MoveProgress moveTo(MovingRoadUser object, RoadUser destination,
            TimeLapse time) {
        return moveTo(object, getPosition(destination), time);
    }

    /**
     * Should be overriden by subclasses to define actual
     * {@link RoadModel#followPath(MovingRoadUser, Queue, TimeLapse)} behavior.
     * @param object The object that is moved.
     * @param path The path that is followed.
     * @param time The time that is available for travel.
     * @return A {@link MoveProgress} instance containing the actual travel
     *         details.
     */
    protected abstract MoveProgress doFollowPath(MovingRoadUser object,
            Queue<Point> path, TimeLapse time);

    protected void addObjectAt(RoadUser newObj, Point pos) {
        checkArgument(!objLocs.containsKey(newObj), "Object is already added");
        objLocs.put(newObj, point2LocObj(pos));
    }

    protected void addObjectAtSamePosition(RoadUser newObj, RoadUser existingObj) {
        checkArgument(!objLocs.containsKey(newObj), "Object " + newObj
                + " is already added.");
        checkArgument(objLocs.containsKey(existingObj), "Object " + existingObj
                + " does not exist.");
        objLocs.put(newObj, objLocs.get(existingObj));
    }

    protected void removeObject(RoadUser roadUser) {
        checkArgument(roadUser != null, "RoadUser can not be null");
        checkArgument(objLocs.containsKey(roadUser), "RoadUser: " + roadUser
                + " does not exist.");
        objLocs.remove(roadUser);
        objDestinations.remove(roadUser);
    }

    @Override
    public void clear() {
        objLocs.clear();
        objDestinations.clear();
    }

    @Override
    public boolean containsObject(RoadUser obj) {
        checkArgument(obj != null, "obj can not be null");
        return objLocs.containsKey(obj);
    }

    @Override
    public boolean containsObjectAt(RoadUser obj, Point p) {
        checkArgument(p != null, "point can not be null");
        if (containsObject(obj)) {
            return objLocs.get(obj).equals(p);
        }
        return false;
    }

    @Override
    public boolean equalPosition(RoadUser obj1, RoadUser obj2) {
        return containsObject(obj1) && containsObject(obj2)
                && getPosition(obj1).equals(getPosition(obj2));
    }

    @Override
    public Map<RoadUser, Point> getObjectsAndPositions() {
        Map<RoadUser, T> copiedMap;
        synchronized (objLocs) {
            copiedMap = new LinkedHashMap<RoadUser, T>();
            copiedMap.putAll(objLocs);
        } // it is save to release the lock now

        final Map<RoadUser, Point> theMap = new LinkedHashMap<RoadUser, Point>();
        for (final java.util.Map.Entry<RoadUser, T> entry : copiedMap
                .entrySet()) {
            theMap.put(entry.getKey(), locObj2point(entry.getValue()));
        }
        return theMap;
    }

    @Override
    public Point getPosition(RoadUser roadUser) {
        checkArgument(roadUser != null, "object can not be null");
        checkArgument(containsObject(roadUser), "RoadUser does not exist");
        return locObj2point(objLocs.get(roadUser));
    }

    @Override
    public Collection<Point> getObjectPositions() {
        return getObjectsAndPositions().values();
    }

    @Override
    public Set<RoadUser> getObjects() {
        synchronized (objLocs) {
            final Set<RoadUser> copy = new LinkedHashSet<RoadUser>();
            copy.addAll(objLocs.keySet());
            return copy;
        }
    }

    @Override
    public Set<RoadUser> getObjects(Predicate<RoadUser> predicate) {
        return Sets.filter(getObjects(), predicate);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <Y extends RoadUser> Set<Y> getObjectsAt(RoadUser roadUser,
            Class<Y> type) {
        checkArgument(roadUser != null, "roadUser can not be null");
        checkArgument(type != null, "type can not be null");
        final Set<Y> result = new HashSet<Y>();
        for (final RoadUser ru : getObjects(new SameLocationPredicate(roadUser,
                type, this))) {
            result.add((Y) ru);
        }
        return result;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <Y extends RoadUser> Set<Y> getObjectsOfType(final Class<Y> type) {
        if (type == null) {
            throw new IllegalArgumentException("type can not be null");
        }
        return (Set<Y>) getObjects(new Predicate<RoadUser>() {
            @Override
            public boolean apply(RoadUser input) {
                return type.isInstance(input);
            }
        });
    }

    @Override
    public List<Point> getShortestPathTo(RoadUser fromObj, RoadUser toObj) {
        checkArgument(fromObj != null, "fromObj can not be null");
        checkArgument(objLocs.containsKey(toObj), " to object should be in RoadModel. "
                + toObj);
        return getShortestPathTo(fromObj, getPosition(toObj));
    }

    @Override
    public List<Point> getShortestPathTo(RoadUser fromObj, Point to) {
        checkArgument(fromObj != null, "fromObj can not be null");
        checkArgument(objLocs.containsKey(fromObj), " from object should be in RoadModel. "
                + fromObj);
        return getShortestPathTo(getPosition(fromObj), to);
    }

    @Override
    public void register(RoadUnit unit) {
        checkArgument(unit != null, "RoadHolder can not be null");
        
        RoadGuard guard;
        
        if( unit instanceof MovingRoadUnit){
            guard = new MovingRoadGuard((MovingRoadUnit) unit, this);
        }
        else {
            guard = new RoadGuard(unit, this);
        }
        addObjectAt(unit.getElement(), unit.getInitData().getStartPosition());
        unit.setRoadAPI(guard);
        mapping.put(unit.getElement(), guard);
    }

    @Override
    public void unregister(RoadUnit holder) {
        checkArgument(holder != null, "RoadHolder can not be null");
        
        if (containsObject(holder.getElement())) {
            removeObject(holder.getElement());
            mapping.remove(holder.getElement());
        }
    }

    @Override
    public final EventAPI getEventAPI() {
        return eventAPI;
    }

    private static class SameLocationPredicate implements Predicate<RoadUser> {
        private final RoadUser reference;
        private final RoadModel model;
        private final Class<?> type;

        public SameLocationPredicate(final RoadUser pReference,
                final Class<?> pType, final RoadModel pModel) {
            reference = pReference;
            type = pType;
            model = pModel;
        }

        @Override
        public boolean apply(RoadUser input) {
            return type.isInstance(input)
                    && model.equalPosition(input, reference);
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
}
