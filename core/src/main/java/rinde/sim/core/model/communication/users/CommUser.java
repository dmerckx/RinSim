package rinde.sim.core.model.communication.users;

import rinde.sim.core.model.communication.apis.CommunicationAPI;
import rinde.sim.core.simulation.types.Agent;


public interface CommUser extends Agent{

	public void setCommunicationAPI(CommunicationAPI api);
	
	
}
