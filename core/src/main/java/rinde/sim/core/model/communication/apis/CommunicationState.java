package rinde.sim.core.model.communication.apis;

import rinde.sim.core.model.communication.Address;

public abstract class CommunicationState {

    CommunicationState() {}
    
    public abstract Address getAddress();
}
