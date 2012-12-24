package rinde.sim.core.model.interaction;

import rinde.sim.core.graph.Point;
import rinde.sim.core.model.communication.Address;
import rinde.sim.core.model.communication.Message;
import rinde.sim.core.model.communication.apis.CommunicationState;
import rinde.sim.core.model.communication.apis.SimpleCommAPI;
import rinde.sim.core.model.communication.users.SimpleCommData;
import rinde.sim.core.model.communication.users.SimpleCommUser;
import rinde.sim.core.model.interaction.users.InteractionUser;

/**
 * Expands the functionality of a common {@link Receiver} by allowing
 * subclasses to send message to the {@link InteractionUser} that
 * advertised this receiver.
 * 
 * @author dmerckx
 */
public abstract class ExtendedReceiver extends Receiver implements SimpleCommUser<SimpleCommData>{

    private Address guard;
    
    /**
     * The communication API, which is used to send messages to the guard
     * that advertised this receiver.
     * It is made protected to allowed implementation of this class to send
     * messages as well if desired.  
     */
    protected SimpleCommAPI commAPI;
    
    /**
     * @param location The location of this receiver.
     */
    @SuppressWarnings("hiding")
    public ExtendedReceiver(Point location) {
        super(location);
    }
    
    /**
     * Can be used to send a notification to the {@link InteractionUser} that
     * advertised this receiver.
     * @param notification The message to be send.
     */
    public final void sendNotification(Message notification){
        commAPI.send(guard, notification);
    }
    
    /**
     * Set the the address of the guard that advertised this receiver.
     * @param guardAddress The address of the advertising guard.
     */
    public final void setGuard(Address guardAddress){
        this.guard = guardAddress;
    }

    @Override
    public final void setCommunicationAPI(SimpleCommAPI api) {
        this.commAPI = api;
    }

    @Override
    public final CommunicationState getCommunicationState() {
        return commAPI.getState();
    }
}
