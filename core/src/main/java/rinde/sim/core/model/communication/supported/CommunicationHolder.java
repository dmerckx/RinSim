package rinde.sim.core.model.communication.supported;

import rinde.sim.core.model.Holder;
import rinde.sim.core.model.communication.guards.CommunicationGuard;
import rinde.sim.core.model.communication.users.CommUser;

public interface CommunicationHolder extends Holder{

    public CommunicationGuard getCommunicationGuard();
    
    public CommUser getElement();

}
