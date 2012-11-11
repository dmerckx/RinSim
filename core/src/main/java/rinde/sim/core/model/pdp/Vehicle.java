/**
 * 
 */
package rinde.sim.core.model.pdp;

import rinde.sim.core.model.road.users.MovingRoadUser;
import rinde.sim.core.simulation.Agent;
import rinde.sim.core.simulation.TimeLapse;

/**
 * Abstract base class for vehicle concept: moving {@link Container}.
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 */
public abstract class Vehicle extends ContainerImpl implements MovingRoadUser,
        Agent {

    @Override
    public final PDPType getType() {
        return PDPType.VEHICLE;
    }

    @Override
    public final void tick(TimeLapse time) {
        // finish previously started pickup and delivery actions that need to
        // consume time
        getPDPModel().continuePreviousActions(this, time);
        tickImpl(time);
    }

    /**
     * Is called every tick. This replaces the
     * {@link Agent#tick(TimeLapse)} for vehicles.
     * @param time The time lapse that can be used.
     * @see Agent#tick(TimeLapse)
     */
    protected abstract void tickImpl(TimeLapse time);
}
