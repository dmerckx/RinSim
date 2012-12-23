package rinde.sim.core.model.communication.apis;

import rinde.sim.core.model.communication.Message;

/**
 * On top of the functionalities provided by the {@link SimpleCommAPI}
 * this API will also provide methods to broadcasts messages to multiple
 * agents.
 * 
 * @author dmerckx
 */
public interface CommAPI extends SimpleCommAPI{

    /**
     * The radius in which broadcast messages, send by this user,
     * will be received.
     * @return  The broadcast radius.
     */
    public double getRadius();
    
    /**
     * Set the radius used for broadcasting messages.
     * @param radius The new broadcast radius.
     */
    void setRadius(double radius);
    
    /**
     * Broadcast a message to everyone within broadcast range.
     * The receivers can read this message from their mailbox the
     * next tick.
     * @param message The message to be broadcasted.
     */
    void broadcast(Message message);
}
