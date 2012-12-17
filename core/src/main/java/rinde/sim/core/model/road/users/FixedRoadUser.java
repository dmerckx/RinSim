package rinde.sim.core.model.road.users;

import rinde.sim.core.model.road.apis.RoadAPI;

public interface FixedRoadUser<D extends RoadData> extends RoadUser<D>{

    public void setRoadAPI(RoadAPI api);
}
