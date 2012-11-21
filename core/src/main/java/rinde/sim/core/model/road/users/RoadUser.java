/** 
 * 
 */
package rinde.sim.core.model.road.users;

import rinde.sim.core.graph.Point;
import rinde.sim.core.model.User;
import rinde.sim.core.model.road.RoadModel;
import rinde.sim.core.model.road.supported.RoadUnit;

/**
 * A RoadUser is an object living on the {@link RoadModel}.
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 */
public interface RoadUser extends User{
    
    public RoadData initData();
    
    public RoadUnit buildUnit();
    
   
    public interface RoadData {

        Point getStartLocation();
    }
}
