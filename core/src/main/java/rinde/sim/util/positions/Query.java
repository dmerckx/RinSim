package rinde.sim.util.positions;

import rinde.sim.core.model.road.users.RoadUser;

public interface Query {
    
    /**
     * Return true if value should be filtered out of the results
     */
    void process(RoadUser<?> t);
    
    public Class<?> getType();
}
