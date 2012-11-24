package rinde.sim.core.model.road.guards;

import rinde.sim.core.graph.Point;
import rinde.sim.core.model.road.RoadModel;
import rinde.sim.core.model.road.apis.RoadAPI;
import rinde.sim.core.model.road.supported.RoadUnit;
import rinde.sim.core.simulation.TimeInterval;

public class RoadGuard implements RoadAPI{

    protected RoadModel model;
    
    protected Point location;
    protected Point lastLocation;
    
    public RoadGuard(RoadUnit unit, RoadModel model) {
        this.model = model;
        
        this.location = unit.getInitData().getStartPosition();
        this.lastLocation = location;
    }

    @Override
    public Point getLocation() {
        return location;
    }

    @Override
    public Point getLastLocation() {
        return lastLocation;
    }
    
    public void afterTick(TimeInterval interval){
        lastLocation = location;
    }
}
