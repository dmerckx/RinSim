package rinde.sim.util;

import rinde.sim.core.model.road.RoadModel;
import rinde.sim.core.model.road.users.MovingRoadUser;

/**
 * 
 * Ignores the model registration.
 * @author Bartosz Michalik <bartosz.michalik@cs.kuleuven.be>
 * 
 */
public class TrivialRoadUser implements MovingRoadUser {

	private RoadModel model;

	public RoadModel getRoadModel() {
		return model;
	}

	@Override
	public void initRoadUser(RoadModel m) {
		model = m;
	}

	@Override
	public double getSpeed() {
		return 1;
	}
}
