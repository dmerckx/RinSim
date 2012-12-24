package rinde.sim.core.model.communication.users;

import rinde.sim.core.model.communication.apis.CommAPI;
import rinde.sim.core.model.road.users.RoadUser;
import rinde.sim.core.simulation.Simulator;


/**
 * Represents a full communication user, able to send, receive and broadcast
 * messages.
 * Whenever a communication user is registered in the {@link Simulator}
 * it will be assigned a {@link CommAPI}.
 * This API will be set via the {@link CommUser setCommunicationAPI}
 * method, and can be used thereafter to send and receive messages.
 * 
 * @author dmerckx
 *
 * @param <D> The type of initialization data.
 */
public interface FullCommUser<D extends CommData> extends RoadUser<D>, CommUser<D>{

	/**
	 * Sets the communication API of this user.
	 * 
	 * Note:
	 * This method should simply store the given API.
	 * No side effects should be applied during this call.
	 * 
	 * @param api The communication API.
	 */
	public void setCommunicationAPI(CommAPI api);
}