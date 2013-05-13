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
}
