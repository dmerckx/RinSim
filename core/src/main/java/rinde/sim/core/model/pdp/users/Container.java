package rinde.sim.core.model.pdp.users;

import rinde.sim.core.model.interaction.users.InteractionUser;
import rinde.sim.core.model.pdp.apis.ContainerAPI;
import rinde.sim.core.model.road.users.RoadUser;
import rinde.sim.core.old.pdp.Parcel_Old;
import rinde.sim.core.simulation.Simulator;

/**
 * A type of container, implementations of this interface will be
 * provided with a {@link ContainerAPI} when registered into the
 * {@link Simulator}.
 * 
 * The {@link ContainerAPI} contains the necessary methods for this
 * class to function, after initialization it can potentially hold a
 * load of {@link Parcel}s and will provide ways to pickup/deliver them.
 * 
 * @author dmerckx
 *
 * @param <P> The type of parcels contained.
 */
public interface Container<D extends ContainerData>
            extends RoadUser<D>, InteractionUser<D>, PdpUser<D>{
    
    void setContainerAPI(ContainerAPI api);
}
