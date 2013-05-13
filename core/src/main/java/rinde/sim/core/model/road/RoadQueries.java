package rinde.sim.core.model.road;

import java.util.Set;

import rinde.sim.core.graph.Point;
import rinde.sim.core.model.SafeIterator;
import rinde.sim.core.model.road.users.FixedRoadUser;
import rinde.sim.core.model.road.users.MovingRoadUser;
import rinde.sim.core.model.road.users.RoadUser;
import rinde.sim.util.positions.Query;

import com.google.common.base.Predicate;

/**
 * @author merckx
 */
public interface RoadQueries {
    /**
     * 
     * @param pos
     * @param range
     * @param q
     */
    <T extends RoadUser<?>> void queryAround(Point pos, double range, Query<T> q);
    
    /**
     * Returns a safe iterator over all the road users active in the simulation.
     * 
     * Important Note:
     * This is not a scalable solution, use local broadcasts instead when possible.
     * The obtained result should not be casted down! Doing so could break determinacy. 
     * @return An iterator to access all road users.
     */
    SafeIterator<RoadUser<?>> queryRoadUsers();
    
    /**
     * Returns a safe iterator over all the fixed road users active in the simulation.
     * 
     * Important Note:
     * This is not a scalable solution, use local broadcasts instead when possible.
     * The obtained result should not be casted down! Doing so could break determinacy. 
     * @return An iterator to access all road users.
     */
    SafeIterator<FixedRoadUser<?>> queryFixedRoadUsers();
    
    /**
     * Returns a safe iterator over all the moving road users active in the simulation.
     * 
     * Important Note:
     * This is not a scalable solution, use local broadcasts instead when possible.
     * The obtained result should not be casted down! Doing so could break determinacy. 
     * @return An iterator to access all road users.
     */
    SafeIterator<MovingRoadUser<?>> queryMovingRoadUsers();

    /**
     * This method returns a set of {@link RoadUser} objects which exist in this
     * model and satisfy the given {@link Predicate}. The returned set is not a
     * live view on this model, but a new created copy.
     * @param predicate The predicate that decides which objects to return.
     * @return A set of {@link RoadUser} objects.
     */
    Set<RoadUser<?>> getRoadUsers(Predicate<RoadUser<?>> predicate);

    /**
     * Returns all objects of the given type located in the same position as the
     * given {@link RoadUser}.
     * @param roadUser The object which location is checked for other objects.
     * @param type The type of the objects to be returned.
     * @return A set of objects of type <code>type</code>.
     */
    <Y extends RoadUser<?>> Set<Y> getObjectsAt(Point location, Class<Y> type);

    /**
     * This method returns a set of {@link RoadUser} objects which exist in this
     * model and are instances of the specified {@link Class}. The returned set
     * is not a live view on the set, but a new created copy.
     * @param type The type of returned objects.
     * @return A set of {@link RoadUser} objects.
     */
    <Y extends RoadUser<?>> Set<Y> getObjectsOfType(final Class<Y> type);
}
