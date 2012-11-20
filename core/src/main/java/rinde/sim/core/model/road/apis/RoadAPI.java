package rinde.sim.core.model.road.apis;

import rinde.sim.core.graph.Point;
import rinde.sim.core.refs.RefBackup;

public interface RoadAPI {
    
    public RefBackup<Point> getPosition();
}
