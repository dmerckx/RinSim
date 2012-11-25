package rinde.sim.core.model.road.dummies;

import rinde.sim.core.graph.Point;
import rinde.sim.core.model.UnitImpl;
import rinde.sim.core.model.road.apis.MovingRoadAPI;
import rinde.sim.core.model.road.apis.RoadAPI;
import rinde.sim.core.model.road.supported.MovingRoadUnit;

public class TrivialRoadUnit extends UnitImpl implements MovingRoadUnit{

    public final TrivialRoadUser user;
    public final Point location;
    public final double speed;
    
    public MovingRoadAPI roadAPI;
    
    public TrivialRoadUnit(TrivialRoadUser user) {
        this(user, new Point(0,0), 1.0d);
    }
    
    public TrivialRoadUnit(TrivialRoadUser user, Point location, double speed) {
        this.user = user;
        this.location = location;
        this.speed = speed;
    }
    
    @Override
    public void setRoadAPI(RoadAPI api) {
        this.roadAPI = (MovingRoadAPI) api;
    }

    @Override
    public void init() {
        user.roadAPI = this.roadAPI;
    }

    @Override
    public MovingRoadAPI getRoadAPI() {
        return roadAPI;
    }

    @Override
    public TrivialRoadUser getElement() {
        return user;
    }

    @Override
    public MovingRoadData getInitData() {
        return new MovingRoadData(){
            @Override
            public Point getStartPosition() {
                return location;
            }

            @Override
            public Double getInitialSpeed() {
                return speed;
            }
        };
    }

}
