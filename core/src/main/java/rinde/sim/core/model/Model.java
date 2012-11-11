package rinde.sim.core.model;

import rinde.sim.core.model.simulator.SimulatorAPI;
import rinde.sim.core.simulation.TickListener;
import rinde.sim.core.simulation.TimeInterval;

/**
 * @author Bartosz Michalik <bartosz.michalik@cs.kuleuven.be>
 * @param <T> basic type of element supported by model
 */
public interface Model<T>{
    
    /**
     * Through this method the user of the simulator receives a reference to the
     * {@link SimulatorAPI}.
     * @param api The simulator which this uses gets access to.
     */
    public void setSimulatorAPI(SimulatorModelAPI api);

    /**
     * Register element in a model.
     * @param element the <code>! null</code> should be imposed
     */
    void register(T element);

    /**
     * Unregister element from a model.
     * @param element the <code>! null</code> should be imposed
     */
    void unregister(T element);
    
    /**
     * @return The class of the type supported by this model.
     */
    Class<T> getSupportedType();
}
