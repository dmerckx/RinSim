package rinde.sim.core.model.road.apis;

import java.io.Serializable;
import java.util.List;

import rinde.sim.core.graph.Point;
import rinde.sim.core.model.road.Visitor;
import rinde.sim.core.model.road.users.RoadUser;
import rinde.sim.core.refs.RefBackup;

public interface RoadAPI {
    
    public RefBackup<Point> getPosition();
    
    /**
     * Unregister this agent from the roadmodel. This action will take effect
     * only after the following tick. 
     */
    public void unregister();
    
    /**
     * Visit the current node and apply the the visitor to all present
     * RoadUsers of the given target type.
     * @param clazz
     * @return
     */
    public <T extends RoadUser, R extends Serializable> R
            visitNode(Visitor<T, R> visitor);
}
