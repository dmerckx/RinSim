/**
 * 
 */
package rinde.sim.core.model.pdp;

import rinde.sim.core.model.road.users.RoadUser;

/**
 * Base interface for objects in {@link PDPModel}. Can be used directly but
 * usually one of its subclasses are used instead:
 * <ul>
 * <li>{@link Vehicle}</li>
 * <li>{@link Parcel}</li>
 * <li>{@link Depot}</li>
 * </ul>
 * 
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 */
public interface PDPObject extends RoadUser {

    /**
     * @return The type of the PDPObject.
     */
    PDPType getType();

    /**
     * Is called when object is registered in {@link PDPModel}.
     * @param model A reference to the model.
     */
    void initPDPObject(PDPModel model);

}
