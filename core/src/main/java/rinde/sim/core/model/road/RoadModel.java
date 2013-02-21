/**
 * 
 */
package rinde.sim.core.model.road;

import java.util.List;
import java.util.Queue;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;

import rinde.sim.core.graph.Point;
import rinde.sim.core.model.Model;
import rinde.sim.core.model.road.users.MovingRoadUser;
import rinde.sim.core.model.road.users.RoadData;
import rinde.sim.core.model.road.users.RoadUser;
import rinde.sim.core.simulation.TimeLapse;
import rinde.sim.event.EventAPI;
import rinde.sim.util.Rectangle;

/**
 * RoadModel is a model that manages a fleet of vehicles ({@link RoadUser}s) on
 * top of a <i>space</i>. The space that is used depends on the specific
 * implementation of {@link RoadModel}. {@link RoadUser}s have a position which
 * is represented by a {@link Point}. Generally, RoadModels are responsible for:
 * <ul>
 * <li>adding and removing objects</li>
 * <li>moving objects around</li>
 * </ul>
 * On top of that the RoadModel provides several functions for retrieving
 * objects and finding the shortest path. More utilities for working with
 * {@link RoadModel}s are defined in {@link RoadModels}.
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 */
public interface RoadModel extends Model<RoadData, RoadUser<?>>, RoadQueries {
    
    /**
     * Moves the specified {@link MovingRoadUser} using the specified path and
     * with the specified time. The provided <code>path</code> can not be empty
     * and there must be time left in the provided {@link TimeLapse}. <br/>
     * <br/>
     * <b>Speed</b><br/>
     * The {@link MovingRoadUser} has to define a speed with which it wants to
     * travel. This method uses the {@link MovingRoadUser}s speed as an
     * <i>upper</i> bound, it gives no guarantee about the lower bound (i.e. the
     * object could stand still). The actual speed of the object depends on the
     * model implementation. A model can define constraints such as speed limits
     * or traffic jams which can slow down a {@link MovingRoadUser}. <br/>
     * <br/>
     * <b>Path</b><br/>
     * The {@link MovingRoadUser} follows the path that is specified by the
     * provided {@link Queue}. This path is composed of a number of
     * {@link Point}s, which will be traveled in order as they appear. For
     * example: consider that the path contains three points:
     * <code>A, B, C</code>. The {@link MovingRoadUser} will first travel to
     * {@link Point} <code>A</code>, once it has reached this point it will be
     * <i>removed</i> out of the {@link Queue}. This means that after this
     * method is finished the provided {@link Queue} will contain only
     * <code>B, C</code>. By storing the reference to the queue, users of this
     * method can repeatedly call this method using the same path object
     * instance. <br/>
     * <br/>
     * <b>Time</b><br/>
     * The time that is specified as indicated by the {@link TimeLapse} object
     * may or may not be consumed completely. Normally, this method will try to
     * consume all time in the {@link TimeLapse} object. In case the end of the
     * path is reached before all time is consumed (which depends on the
     * object's <i>speed</i>, the length of the <code>path</code> and any speed
     * constraints if available) there will be some time left in the
     * {@link TimeLapse}. <br/>
     * @param object The object that is moved.
     * @param path The path that is followed.
     * @param time The time that is available for travel.
     * @return A {@link MoveProgress} instance which details: the distance
     *         traveled, the actual time spent traveling and the nodes which
     *         where traveled.
     * @see #moveTo(MovingRoadUser, Point, TimeLapse)
     * @see #moveTo(MovingRoadUser, RoadUser, TimeLapse)
     */
    MoveProgress followPath(MovingRoadUser<?> object, Queue<Point> path,
            TimeLapse time);

    /**
     * Method to retrieve the location of an object.
     * @param roadUser The object for which the position is examined.
     * @return The position (as a {@link Point} object) for the specified
     *         <code>obj</code> object.
     */
    Point getPosition(RoadUser<?> roadUser);

    /**
     * Convenience method for {@link #getShortestPathTo(Point, Point)}.
     * @param fromObj The object which is used as the path origin
     * @param to The path destination
     * @return The shortest path from 'fromObj' to 'to'
     */
    List<Point> getShortestPathTo(RoadUser<?> fromObj, Point to);

    // ----- EXTERNAL METHODS ----- //

    /**
     * Searches a random position in the space which is defined by this model.
     * @param rnd The {@link RandomGenerator} which is used for obtaining a
     *            random number.
     * @return A random position in this model.
     */
    Point getRandomPosition(RandomGenerator rnd);
    
    /**
     * This method returns the set of {@link RoadUser} objects which exist in
     * this model. The returned set is not a live view on the set, but a new
     * created copy.
     * Used by the UI to draw road users.
     * @return The set of {@link RoadUser} objects.
     */
    Set<RoadUser<?>> getAllRoadUsers();

    /**
     * Finds the shortest between <code>from</code> and <code>to</code>. The
     * definition of a <i>shortest</i> path is defined by the specific
     * implementation, possiblities include the shortest travel time and the
     * shortest distance.
     * @param from The start point of the path.
     * @param to The end point of the path.
     * @return The shortest path.
     */
    List<Point> getShortestPathTo(Point from, Point to);
    
    Rectangle getViewRect();
    
    EventAPI getEventAPI();
}