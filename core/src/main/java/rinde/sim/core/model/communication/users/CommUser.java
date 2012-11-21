package rinde.sim.core.model.communication.users;

import rinde.sim.core.model.communication.supported.CommUnit;
import rinde.sim.core.model.road.users.RoadUser;
import rinde.sim.core.simulation.types.Agent;


public interface CommUser extends RoadUser, Agent{

	public CommData initData();
	
	public CommUnit buildUnit();
	
	
	public interface CommData extends RoadData{

	    Double getRadius();
	    
	    Double getReliability();
	}
}
