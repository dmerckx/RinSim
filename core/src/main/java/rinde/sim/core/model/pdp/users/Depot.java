package rinde.sim.core.model.pdp.users;

import rinde.sim.core.model.pdp.Parcel;
import rinde.sim.core.model.pdp.apis.ContainerAPI;
import rinde.sim.core.model.road.apis.RoadAPI;

public interface Depot<P extends Parcel> extends Container<P>{

    public void setRoadAPI(RoadAPI api);
    
    /**
     * Initialize this container by providing the {@link ContainerAPI}.
     * The {@link ContainerAPI init} has to be called in this method,
     * otherwise a runtime exception can be thrown.
     * 
     * @param api The api proving the required functionality for this container
     */
    public void setContainerAPI(ContainerAPI<P> api); 
}
