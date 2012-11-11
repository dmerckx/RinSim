package rinde.sim.core.model.road.users;

import rinde.sim.core.model.road.apis.FixedRoadAPI;

public interface FixedRoadUser extends RoadUser{

    public void initRoadUser(FixedRoadAPI api);
}
