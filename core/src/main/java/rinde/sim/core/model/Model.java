package rinde.sim.core.model;

import java.util.List;

import rinde.sim.core.simulation.TimeInterval;
import rinde.sim.core.simulation.UserInit;


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
    List<UserInit<?>> register(T user, D data);
    
    void setSeed(long seed);
    
    /**
     * Unregister element from a model.
     * @param element the <code>! null</code> should be imposed
     */
    List<User<?>> unregister(T user);
    
    /**
     * @return The class of the type supported by this model.
     */
    Class<T> getSupportedType();
    
    
    void tick(TimeInterval time);
}
