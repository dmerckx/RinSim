package rinde.sim.core.model.road.users;

import rinde.sim.core.model.road.apis.MovingRoadAPI;

public interface MovingRoadUser<D extends MovingRoadData> extends RoadUser<D> {

    public void setRoadAPI(MovingRoadAPI api);
}
