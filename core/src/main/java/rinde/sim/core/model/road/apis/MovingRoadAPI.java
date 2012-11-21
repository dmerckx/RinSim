package rinde.sim.core.model.road.apis;

import java.util.Queue;

import rinde.sim.core.graph.Point;
import rinde.sim.core.model.road.InvalidLocationException;
import rinde.sim.core.simulation.TimeLapse;

public interface MovingRoadAPI extends RoadAPI{
       
    public void setSpeed(double speed);
    
    public double getSpeed();
    
    /**
     * Returns a random, reachable location on the map.
     */
    public Point getRandomLocation();
    
    /**
     * Take the shortest path to the given location.
     */
    public void setTarget(Point p) throws InvalidLocationException;
    
    /**
     * Drive the given path.
     */
    public void setTarget(Queue<Point> path) throws InvalidLocationException;
    
    /**
     * Advance in time, if the target is reached within the given TimeLapse,
     * a part of this TimeLapse will be left, otherwise it will be completely
     * consumed.
     */
    public void advance(TimeLapse time);
    
    /**
     * Check whether or not the given target has already been reached.
     */
    public boolean isDriving();
}
