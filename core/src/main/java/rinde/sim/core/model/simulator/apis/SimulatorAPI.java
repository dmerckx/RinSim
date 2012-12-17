package rinde.sim.core.model.simulator.apis;


import rinde.sim.core.model.Data;
import rinde.sim.core.model.User;
import rinde.sim.core.simulation.Simulator;

/**
 * Limited simulator API that provides an API for simulation elements (e.g.,
 * agents).
 * @author Bartosz Michalik <bartosz.michalik@cs.kuleuven.be>
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 * @author David Merckx <david.merckx@students.kuleuven.be>
 * @since 2.0
 * 
 */
public interface SimulatorAPI {

    /**
     * Attempts to register a given entity in the simulator at the end of this tick.
     * During registration the object is provided all features it requires
     * (declared by interfaces) and bound to the required models
     * (if they were registered in the simulator before).
     * @param o object to register
     * @throws IllegalStateException when simulator is not configured (by
     *             calling {@link Simulator#configure()}
     */
    <D extends Data> void registerUser(User<D> user, D data);

    /**
     * Attempts to unregister an object from simulator at the end of this tick.
     * @param o The object to be unregistered.
     */
    void unregisterUser(User<?> user);
}
