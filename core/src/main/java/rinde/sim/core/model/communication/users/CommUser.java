package rinde.sim.core.model.communication.users;

import rinde.sim.core.model.Agent;
import rinde.sim.core.model.communication.supported.CommUnit;
import rinde.sim.core.model.road.users.RoadUser;


public interface CommUser extends RoadUser, Agent{

	public CommUnit buildUnit();
}
