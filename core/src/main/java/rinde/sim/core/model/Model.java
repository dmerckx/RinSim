package rinde.sim.core.model;

import rinde.sim.core.simulation.SimulatorToModelAPI;
import rinde.sim.core.simulation.TimeInterval;


/**
 * @author Bartosz Michalik <bartosz.michalik@cs.kuleuven.be>
 * @param <T> basic type of element supported by model
 */
public interface Model<D extends Data, T extends User<? extends D>>{
    
    /**
     * Register element in a model. 
     * 
     * @param element the <code>! null</code> should be imposed
     */
    public void register(SimulatorToModelAPI sim, T user, D data);
    
    /**
     * Unregister element from a model.
     * @param element the <code>! null</code> should be imposed
     */
    void unregister(T user);
    
    /**
     * @return The class of the type supported by this model.
     */
    Class<T> getSupportedType();
    
    
    void tick(TimeInterval time);
}
