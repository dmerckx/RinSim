package rinde.sim.core.model.communication.users;

import rinde.sim.core.model.communication.apis.CommAPI;
import rinde.sim.core.model.communication.apis.CommunicationState;
import rinde.sim.core.model.communication.apis.SimpleCommAPI;
import rinde.sim.core.simulation.Simulator;

/**
 * Represents a simple communication user, able to send and receive messages. 
 * Whenever a communication user is registered in the {@link Simulator}
 * it will be assigned a {@link CommAPI}.
 * This API will be set via the {@link CommUser setCommunicationAPI}
 * method, and can be used thereafter to send and receive messages.
 * 
 * @author dmerckx
 *
 * @param <D> The type of initialization data.
 */
public interface SimpleCommUser<D extends SimpleCommData> extends CommUser<D>{

    /**
     * Sets the communication API of this user.
     * 
     * Note:
     * This method should simply store the given API.
     * No side effects should be applied during this call.
     * 
     * @param api The communication API.
     */
    public void setCommunicationAPI(SimpleCommAPI api);

    
    public static class Std implements SimpleCommUser<SimpleCommData>{
        protected SimpleCommAPI commAPI;
        
        @Override
        public void setCommunicationAPI(SimpleCommAPI api) {
            this.commAPI = api;
        }
        
        @Override
        public CommunicationState getCommunicationState() {
            return commAPI.getState();
        }
    }
}
