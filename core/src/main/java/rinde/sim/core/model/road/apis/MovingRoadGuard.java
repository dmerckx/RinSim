package rinde.sim.core.model.road.apis;

import java.util.LinkedList;
import java.util.Queue;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;

import rinde.sim.core.graph.Point;
import rinde.sim.core.model.road.InvalidLocationException;
import rinde.sim.core.model.road.RoadModel;
import rinde.sim.core.model.road.users.MovingRoadData;
import rinde.sim.core.model.road.users.MovingRoadUser;
import rinde.sim.core.simulation.TimeLapse;
import rinde.sim.core.simulation.time.TimeLapseHandle;

import com.google.common.collect.Lists;

/**
 * An implementation of the {@link MovingRoadAPI}.
 * 
 * This guard guarantees additional location consistency:
 *  - the position of this API will never change more then the 
 *  maximal speed provided * the amount of time consumed
 *  - the current position of this APi will always be a valid position 
 * 
 * @author dmerckx
 */
public class MovingRoadGuard extends RoadGuard implements MovingRoadAPI{

    private Queue<Point> path = Lists.newLinkedList();
    private final RandomGenerator rnd;
    
    private long lastChangedTime;
    private double speed;
    private final TimeLapseHandle handle;
    
    public Point getTarget(){
        if(path != null && !path.isEmpty()){
            return ((LinkedList<Point>) path).getLast();
        }
        return getCurrentLocation();
    }
    
    /**
     * Construct a new guard. 
     * @param user The user to which this API belongs.
     * @param data The initialization data for this API.
     * @param model The road model.
     * @param seed The seed used for generating random number.
     * @param handle A handle to the users time lapse.
     */
    @SuppressWarnings("hiding")
    public MovingRoadGuard(MovingRoadUser<?> user, MovingRoadData data, RoadModel model, long seed, TimeLapseHandle handle) {
        super(user, data, model);
        this.rnd = new MersenneTwister(seed);
        this.speed = data.getInitialSpeed();
        this.handle = handle;
    }
    
    // ------ MOVING ROAD API ------ //
    
    @SuppressWarnings("hiding")
    @Override
    public void setSpeed(double speed){
        this.speed = speed;
    }

    @Override
    public double getSpeed(){
        return speed;
    }
    
    @Override
    public Queue<Point> getPath() {
        return path;
    }
    
    @Override
    public Point getLocation() {
        if(handle.getStartTime() > lastChangedTime){
            lastLocation = getCurrentLocation();
            lastChangedTime = handle.getStartTime();
        }
        
        return lastLocation;
    }

    @Override
    public Point getCurrentLocation() {
        return model.getPosition(user);
    }

    @Override
    public Point getRandomLocation() {
        return model.getRandomPosition(rnd);
    }

    @Override
    public void setTarget(Point p) throws InvalidLocationException {
        path.clear();
        path.addAll(model.getShortestPathTo(user, p));
    }

    @SuppressWarnings("hiding")
    @Override
    public void setTarget(Queue<Point> path) throws InvalidLocationException {
        this.path = path;
    }

    @Override
    public void advance(TimeLapse lapse) {
        if(handle.getStartTime() > lastChangedTime){
            lastLocation = getCurrentLocation();
            lastChangedTime = handle.getStartTime();
        }
        
        if(! isDriving() || ! lapse.hasTimeLeft()) return;
        model.followPath((MovingRoadUser<?>) user, path, lapse);
    }

    @Override
    public boolean isDriving() {
        return !path.isEmpty();
    }
}