package rinde.sim.core.model.simulator.users;

import rinde.sim.core.model.Data;
import rinde.sim.core.model.User;
import rinde.sim.core.model.simulator.apis.SimulatorAPI;

/**
 * A simulator user, able to register and unregister agents.
 * 
 * Note that the features provided here should be used with caution!
 * By registering or unregistering agents the general contract,
 * stating that agents should never have references to any other
 * agents in the simulator, is violated.
 * In general deterministic execution can not be guaranteed when
 * this contract is violated.
 * 
 * This interface is merely provided for convenience.
 *  
 * 
 * @author dmerckx
 *
 * @param <D> The type of initialization data.
 */
public interface SimulatorUser<D extends Data> extends User<D>{

    /**
     * Sets the simulator API of this user.
     * 
     * Note:
     * This method should simply store the given API.
     * No side effects should be applied during this call.
     * 
     * @param api The simulator API.
     */
    void setSimulatorAPI(SimulatorAPI api);
}
