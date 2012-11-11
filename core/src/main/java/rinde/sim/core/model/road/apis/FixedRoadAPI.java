package rinde.sim.core.model.road.apis;

import rinde.sim.core.graph.Point;
import rinde.sim.core.refs.ConstantRef;

public interface FixedRoadAPI extends RoadAPI{

    @Override
    public ConstantRef<Point> getPosition();
    
    public void init(Point startLocation);

}
