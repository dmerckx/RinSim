package rinde.sim.core.model.communication.apis;

import java.util.Iterator;

import rinde.sim.core.graph.Point;
import rinde.sim.core.model.communication.Address;
import rinde.sim.core.model.communication.Delivery;
import rinde.sim.core.model.communication.Message;
import rinde.sim.core.refs.Ref;
import rinde.sim.core.refs.RefBackup;


public interface CommunicationAPI {

    void setReliability(double reliability);
    
    void setRadius(double radius);
    
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
