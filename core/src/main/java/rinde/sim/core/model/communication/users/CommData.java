package rinde.sim.core.model.communication.users;

import rinde.sim.core.model.road.users.RoadData;

public interface CommData extends RoadData {

    Double getInitialRadius();
    
    Double getInitialReliability();
}
