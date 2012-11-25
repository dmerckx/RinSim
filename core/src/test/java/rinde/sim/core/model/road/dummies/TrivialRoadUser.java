package rinde.sim.core.model.road.dummies;

import rinde.sim.core.graph.Point;
import rinde.sim.core.model.road.apis.RoadAPI;
import rinde.sim.core.model.road.supported.MovingRoadUnit;
import rinde.sim.core.model.road.users.MovingRoadUser;

public class TrivialRoadUser implements MovingRoadUser{

    public final TrivialRoadUnit unit;
    public RoadAPI roadAPI;
    
    public TrivialRoadUser() {
        this.unit = new TrivialRoadUnit(this);
    }
    
    public TrivialRoadUser(double speed){
        this.unit = new TrivialRoadUnit(this, new Point(0,0), speed);
    }
    
    @Override
    public MovingRoadUnit buildUnit() {
        return unit;
    }

}
