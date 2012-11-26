package rinde.sim.core.model.road.dummies;

import rinde.sim.core.graph.Point;
import rinde.sim.core.model.road.apis.RoadAPI;
import rinde.sim.core.model.road.supported.MovingRoadUnit;
import rinde.sim.core.model.road.users.MovingRoadUser;

public class TrivialRoadUser implements MovingRoadUser{

    public final TrivialRoadUnit unit;
    public RoadAPI roadAPI;
    
    public TrivialRoadUser() {
        this(1, new Point(0,0));
    }
    
    public TrivialRoadUser(double speed){
        this(speed, new Point(0,0));
    }
    
    public TrivialRoadUser(Point location){
        this(1, location);
    }
    
    public TrivialRoadUser(double speed, Point location){
        this.unit = new TrivialRoadUnit(this, location, speed);
    }
    
    @Override
    public MovingRoadUnit buildUnit() {
        return unit;
    }

}
