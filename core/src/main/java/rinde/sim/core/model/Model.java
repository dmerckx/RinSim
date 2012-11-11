package rinde.sim.core.model;

import rinde.sim.core.model.simulator.SimulatorAPI;
import rinde.sim.core.simulation.TickListener;
import rinde.sim.core.simulation.TimeInterval;

/**
 * @author Bartosz Michalik <bartosz.michalik@cs.kuleuven.be>
 * @param <T> basic type of element supported by model
 */
public interface Model<T> extends TickListener {
    
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
     * Is called at the start of each tick. A modelTick differs from 
     * a normal tick in that they are guaranteed to be carried out sequentially,
     * so no measures have to be taken for threadsafety. ModelTicks are carried
     * out in the order that models were added to the simulator.
     * @param time the time at which this tick starts
     */
    void preTick(TimeInterval t);
    
    /**
     * @return The class of the type supported by this model.
     */
    Class<T> getSupportedType();
}
