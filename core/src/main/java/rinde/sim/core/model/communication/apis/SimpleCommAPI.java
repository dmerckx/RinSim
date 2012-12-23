package rinde.sim.core.model.communication.apis;

import java.util.Iterator;

import rinde.sim.core.model.communication.Address;
import rinde.sim.core.model.communication.Delivery;
import rinde.sim.core.model.communication.Message;

/**
 * The API provided to communication users, every communication user
 * will get his own personal API assigned.
 * 
 * This API allows to send and receive messages.
 * Each communication user has it own unique address
 * which can be used for personal communication.
 * 
 * @author dmerckx
 */
public interface SimpleCommAPI {

    /**
     * Returns the unique address corresponding to this communication user.
     * @return Your address.
     */
    Address getAddress();
    
    /**
     * Returns all messages in the form of an iterator. The remove {@link Iterator remove}
     * method of the iterator can be used to safely remove handled messages.
     * When a message is not removed it will again be available when calling
     * this method.
     * @return An iterator containing all received, unremoved, messages.
     */
    Iterator<Delivery> getMessages();
    
    /**
     * Send a personal message to the given destination address.
     * The receiver can read this message from his mailbox the
     * next tick.
     * @param destination The destination address.
     * @param message The messages to be delivered.
     */
    void send(Address destination, Message message);
    
    /**
     * Returns a presentation of the state of the user of this API. 
     * @return  The state of this user.
     */
    CommunicationState getState();

}
