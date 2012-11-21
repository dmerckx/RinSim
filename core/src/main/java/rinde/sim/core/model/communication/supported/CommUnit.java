package rinde.sim.core.model.communication.supported;

import rinde.sim.core.model.communication.apis.CommunicationAPI;
import rinde.sim.core.model.communication.users.CommUser;
import rinde.sim.core.model.road.supported.RoadUnit;

public interface CommUnit extends RoadUnit{

    public CommunicationAPI getCommunicationAPI();
    
    public void setCommunicationAPI(CommunicationAPI api);
    
    public CommUser getElement();
}
