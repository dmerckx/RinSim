package rinde.sim.core.model.communication;

import java.util.Iterator;

import rinde.sim.core.graph.Point;
import rinde.sim.core.refs.Ref;
import rinde.sim.core.refs.RefBackup;


interface CommunicationAPI {
	
	/**
	 * Must be called when the model is set within a CommunicationUser.
	 * The values given will be used throughout the simulator for this agent.
	 * Changing the position, radius or reliability can be done by holding
	 * your own copy of the Value object and modifying this when necessary.
	 * @param pos
	 * 		The position of the CommunicationUser.
	 * @param radius
	 * 		The radius at which the CommunicatorUser can be reached.
	 * @param reliability
	 * 		The reliability of messages received by the CommunicationUser.
	 * 		Must be a value in the range of ]0,1].
	 */
	void init(RefBackup<Point> pos, Ref<Double> radius, Ref<Double> reliability);
	
	/**
	 * Send a message to the given address.
	 */
    void send(Address destination, Message message);
    
    /**
     * Broadcast a message
     */
    void broadcast(Message message);
    
    /**
     * Returns all messages in the form of an iterator. The remove() method
     * of the iterator can be used to safely remove handled messages.
     */
    Iterator<Delivery> getMessages();

    /**
     * Returns your address.
     */
    Address getAddress();
}
