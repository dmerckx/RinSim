package rinde.sim.core.model.communication.users;

import rinde.sim.core.model.Data;
import rinde.sim.core.model.User;
import rinde.sim.core.model.communication.CommunicationModel;
import rinde.sim.core.model.communication.apis.CommunicationState;

/**
 * Represents a user of the {@link CommunicationModel}.
 * 
 * @author dmerckx
 *
 * @param <D> The type of initialization data.
 */
public interface CommUser<D extends Data> extends User<D>{
    
    /**
     * Should return the state, regarding communication, of this user.
     * This state can be found in the injected communication API.
     * 
     * Note:
     * This method should simply return the state, present in the given API.
     * No side effects should be applied during this call.
     * 
     * @return The communication state.
     */
    public CommunicationState getCommunicationState();
}
