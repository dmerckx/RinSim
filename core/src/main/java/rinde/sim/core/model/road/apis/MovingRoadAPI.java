package rinde.sim.core.model.road.apis;

import java.util.Queue;

import rinde.sim.core.graph.Point;
import rinde.sim.core.model.road.InvalidLocationException;
import rinde.sim.core.refs.Ref;
import rinde.sim.core.simulation.TimeLapse;

public interface MovingRoadAPI extends RoadAPI{
        
    /**
     * Must be called when the model is set within a CommunicationUser.
     * The value given for speed will be used throughout the simulator
     * for this agent, changing the speed can be done by holding a copy
     * of the speed object and modifying this when desired.
     * @param startPosition
     *      The starting location of this agent, further location changes
     *      can be retrieved by observing the Position object of this agent.
     * @param speed
     *      The speed at which the agent advances.
     */
    public void init(Point startLocation, Ref<Double> speed, long seed);
    public void init(Point startLocation, double constantSpeed, long seed);
    
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
