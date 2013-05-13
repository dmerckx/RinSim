package rinde.sim.core.model.road.apis;

import java.util.Queue;

import rinde.sim.core.graph.Point;
import rinde.sim.core.simulation.TimeLapse;

/**
 * Expands the functionality of the {@link RoadAPI} by providing
 * methods to users move forward in the road model.
 * 
 * This API allows users to set a target and to actually drive
 * towards this target by consuming time.
 * 
 * @author dmerckx
 */
public interface MovingRoadAPI extends RoadAPI{
    
    /**
     * Returns the current scheduled path to drive.
     * @return The current path.
     */
    Queue<Point> getPath();
    
    /**
     * Change the speed of this user to the given amount.
     * @param speed The new speed to set.
     */
    void setSpeed(double speed);
    
    /**
     * Returns the maximal speed at which this user moves.
     * @return The current max speed.
     */
    double getSpeed();
    
    /**
     * Returns a random, reachable location on the map.
     * @return A random location.
     */
    Point getRandomLocation();
    
    /**
     * Take the shortest path to the given location.
     * @param p The path to travel towards.
     * @throws InvalidLocationException The provided point is invalid.
     */
    void setTarget(Point p);
    
    /**
     * Drive the given path, which must be a collection of valid nodes
     * in the road model.
     * @param path The path that should be traveled.
     * @throws InvalidLocationException The provided path is invalid.
     */
    void setTarget(Queue<Point> path);
    
    /**
     * Advance in time, if the target is reached within the given TimeLapse,
     * a part of the timelapse will be left, otherwise it will be completely
     * consumed.
     * @param time The timelapse used to consume.
     */
    void advance(TimeLapse time);
    
    /**
     * Check whether or not the given target has already been reached.
     * @return The current target is reached.
     */
    boolean isDriving();
}
