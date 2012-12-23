package rinde.sim.core.model.communication.users;

import rinde.sim.core.model.Data;
import rinde.sim.core.model.User;
import rinde.sim.core.model.communication.CommunicationModel;

/**
 * Represents a user of the {@link CommunicationModel}.
 * 
 * @author dmerckx
 *
 * @param <D> The type of initialization data.
 */
public interface CommUser<D extends Data> extends User<D>{

}
