package rinde.sim.examples.common;

import rinde.sim.core.model.Agent;
import rinde.sim.core.model.road.apis.MovingRoadAPI;
import rinde.sim.core.model.road.apis.RoadState;
import rinde.sim.core.model.road.users.MovingRoadData;
import rinde.sim.core.model.road.users.MovingRoadUser;

public abstract class MovingAgent implements MovingRoadUser<MovingRoadData>, Agent{

	public final String name;
	protected MovingRoadAPI roadAPI;
	
	public MovingAgent(String name) {
		this.name = name;
	}
	
	public MovingAgent() {
		this.name = super.toString();
	}
	
	@Override
	public RoadState getRoadState() {
		return roadAPI.getState();
	}

	@Override
	public void setRoadAPI(MovingRoadAPI api) {
		this.roadAPI = api;
	}
	
	@Override
	public String toString() {
		return super.toString();
	}

}
