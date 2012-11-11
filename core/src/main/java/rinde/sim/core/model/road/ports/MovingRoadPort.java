package rinde.sim.core.model.road.ports;

import java.util.Queue;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;

import rinde.sim.core.graph.Point;
import rinde.sim.core.model.road.InvalidLocationException;
import rinde.sim.core.model.road.RoadModel;
import rinde.sim.core.model.road.apis.MovingRoadAPI;
import rinde.sim.core.model.road.users.MovingRoadUser;
import rinde.sim.core.refs.ConstantRef;
import rinde.sim.core.refs.Ref;
import rinde.sim.core.refs.RefBackup;
import rinde.sim.core.simulation.Port;
import rinde.sim.core.simulation.TimeInterval;
import rinde.sim.core.simulation.TimeLapse;

public class MovingRoadPort extends RoadPort implements Port, MovingRoadAPI, RefBackup<Point> {

    private MovingRoadUser user;

    private Point lastLoc;
    
    private Ref<Double> speed;
    private RandomGenerator rnd;
    
    private Queue<Point> path;
    
    private boolean initialised = false;
    
    public MovingRoadPort(MovingRoadUser user, RoadModel model) {
        super(model);
        this.user = user;
    }

    public double getSpeed(){
        return speed.getValue();
    }
    
    @Override
    public boolean isInitialised(){
        return initialised;
    }
    
    @Override
    public MovingRoadUser getUser() {
        return user;
    }

    @Override
    public void afterTick(TimeInterval l) {
        lastLoc = model.getPosition(user);
    }
    
    // ------ ROAD API ------ //
    
    @Override
    public RefBackup<Point> getPosition() {
        return this;
    }
    
    
    // ------ MOVING ROAD API ------ //

    @Override
    public void init(Point startLocation, Ref<Double> speed, long seed) {
        if(initialised) throw new IllegalStateException("Already initialised");
        
        this.lastLoc = startLocation;
        this.rnd = new MersenneTwister(seed);
        this.speed = speed;
        
        initialised = true;
    }

    @Override
    public void init(Point startLocation, double constantSpeed, long seed) {
        init(startLocation, new ConstantRef<Double>(constantSpeed), seed);
    }

    @Override
    public Point getRandomLocation() {
        return model.getRandomPosition(rnd);
    }

    @Override
    public void setTarget(Point p) throws InvalidLocationException {
        path.clear();
        path.add(p);
    }

    @Override
    public void setTarget(Queue<Point> path) throws InvalidLocationException {
        this.path = path;
    }

    @Override
    public void advance(TimeLapse time) {
        if(! isDriving() || ! time.hasTimeLeft()) return;
        
        model.followPath(user, path, time);
    }

    @Override
    public boolean isDriving() {
        return !path.isEmpty();
    }
    
    
    // ----- BACKUP REF ----- //

    @Override
    public Point getLastValue() {
        return lastLoc;
    }
}
