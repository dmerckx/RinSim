package rinde.sim.examples.common;

import rinde.sim.core.model.road.apis.RoadAPI;
import rinde.sim.core.model.road.apis.RoadState;
import rinde.sim.core.model.road.users.FixedRoadUser;
import rinde.sim.core.model.road.users.RoadData;

public class Package implements FixedRoadUser<RoadData> {
	
	public final String name;
	protected RoadAPI roadAPI;
	
	public Package(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return name;
	}

	@Override
	public void setRoadAPI(RoadAPI api) {
		this.roadAPI = api;
	}

	@Override
	public RoadState getRoadState() {
		return roadAPI.getState();
	}
}