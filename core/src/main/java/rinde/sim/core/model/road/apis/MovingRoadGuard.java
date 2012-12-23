package rinde.sim.core.model.road.apis;

import java.util.Queue;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;

import rinde.sim.core.graph.Point;
import rinde.sim.core.model.Data;
import rinde.sim.core.model.User;
import rinde.sim.core.model.road.InvalidLocationException;
import rinde.sim.core.model.road.RoadModel;
import rinde.sim.core.model.road.users.MovingRoadData;
import rinde.sim.core.model.road.users.MovingRoadUser;
import rinde.sim.core.simulation.TimeLapse;

import com.google.common.collect.Lists;

public class MovingRoadGuard extends RoadGuard implements MovingRoadAPI, User<Data>{

    private RandomGenerator rnd = new MersenneTwister();//TODO
    private Queue<Point> path = Lists.newLinkedList();
    
    public long lastChangedTime;
    public double speed;
    
    public MovingRoadGuard(MovingRoadUser<?> user, MovingRoadData data, RoadModel model) {
        super(user, data, model);
        this.speed = data.getInitialSpeed();
    }
    
    public void setSpeed(double speed){
        this.speed = speed;
    }

    public double getSpeed(){
        return speed;
    }
    
    // ------ MOVING ROAD API ------ //
    
    @Override
    public Queue<Point> getPath() {
        return path;
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

    @Override
    public void setTarget(Queue<Point> path) throws InvalidLocationException {
        this.path = path;
    }

    @Override
    public void advance(TimeLapse time) {
        if(! isDriving() || ! time.hasTimeLeft()) return;
        model.followPath((MovingRoadUser) user, path, time);
        lastChangedTime = time.getStartTime();
    }

    @Override
    public boolean isDriving() {
        return !path.isEmpty();
    }
}
