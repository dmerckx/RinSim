package rinde.sim.core.model.simulator.apis;


import rinde.sim.core.model.Data;
import rinde.sim.core.model.User;
import rinde.sim.core.model.simulator.users.SimulatorUser;

/**
 * The API provided to {@link SimulatorUser}s.
 * 
 * This API allows agents to register new agents during execution.
 * Newly added agents will be added to the simulator at the end
 * of the tick.
 * 
 * 
 * @author dmerckx
 */
public interface SimulatorAPI {

    /**
     * Registers the given user in the simulator at the end of this tick.
     * During registration the object is provided all features it requires
     * (declared by interfaces) and bound to the required models
     * (if they were registered in the simulator before).
     * @param user user to register
     * @param data initialization data for this user
     */
    <D extends Data> void registerUser(User<D> user, D data);

    /**
     * Unregisters the given user from the simulator at the end of this tick.
     * @param user The user to be unregistered.
     */
    void unregisterUser(User<?> user);
}
