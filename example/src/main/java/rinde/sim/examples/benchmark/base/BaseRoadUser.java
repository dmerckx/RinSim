package rinde.sim.examples.benchmark.base;

import rinde.sim.core.model.Agent;
import rinde.sim.core.model.road.apis.MovingRoadAPI;
import rinde.sim.core.model.road.apis.RoadState;
import rinde.sim.core.model.road.users.MovingRoadData;
import rinde.sim.core.model.road.users.MovingRoadUser;
import rinde.sim.core.simulation.TimeLapse;


class BaseRoadUser implements MovingRoadUser<MovingRoadData>, Agent{

	private MovingRoadAPI api;
	
	@Override
	public RoadState getRoadState() {
		return api.getState();
	}

	@Override
	public void tick(TimeLapse time) {
		api.advance(time);	//Drive as far as possible
		
		if(!api.isDriving()){
			//Choose a new random target
			api.setTarget(api.getRandomLocation());
		}
	}

	@Override
	public void setRoadAPI(MovingRoadAPI api) {
		this.api = api;
	}
	
}