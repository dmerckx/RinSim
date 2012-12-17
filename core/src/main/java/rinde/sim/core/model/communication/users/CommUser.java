package rinde.sim.core.model.communication.users;

import rinde.sim.core.model.communication.apis.CommunicationAPI;
import rinde.sim.core.model.communication.apis.CommunicationState;
import rinde.sim.core.model.road.users.RoadUser;


public interface CommUser<D extends CommData> extends RoadUser<D>{

	public void SetCommunicationAPI(CommunicationAPI api);
	
	public CommunicationState getCommunicationState();
}