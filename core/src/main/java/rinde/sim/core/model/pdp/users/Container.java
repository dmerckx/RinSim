package rinde.sim.core.model.pdp.users;

import rinde.sim.core.model.interaction.users.InteractiveAgent;
import rinde.sim.core.model.pdp.apis.ContainerAPI;
import rinde.sim.core.model.pdp.supported.Parcel;
import rinde.sim.core.model.road.users.RoadUser;
import rinde.sim.core.old.pdp.Parcel_Old;
import rinde.sim.core.simulation.Simulator;
import rinde.sim.core.simulation.types.Agent;

/**
 * A type of container, implementations of this interface will be
 * provided with a {@link ContainerAPI} when registered into the
 * {@link Simulator}.
 * 
 * The {@link ContainerAPI} contains the necessary methods for this
 * class to function, after initialization it can potentially hold a
 * load of {@link Parcel_Old}s and will provide ways to pickup/deliver them.
 * 
 * @author dmerckx
 *
 * @param <P> The type of parcels contained.
 */
public interface Container<P extends Parcel> extends RoadUser, InteractiveAgent{
   
    /**
     * Initialize this container by providing the {@link ContainerAPI}.
     * The {@link ContainerAPI init} has to be called in this method,
     * otherwise a runtime exception can be thrown.
     * 
     * @param api The api proving the required functionality for this container
     */
    public void setContainerAPI(ContainerAPI<P> api); 
}
