package rinde.sim.core.simulation.policies;

import java.util.ArrayList;
import java.util.List;

import rinde.sim.core.simulation.TickListener;

/**
 * A serial implementation of a general policy. Listeners are stored
 * in an list and are executed in the same order they were added to
 * this policy.
 * 
 * @author dmerckx
 * @param <T> The type of tick listeners accepted by this policy.
 */
public abstract class Serial<T extends TickListener<?>> implements TickPolicy<T>{

    private boolean register;
    private Class<T> acceptedClass;
    
    /**
     * The listeners of this policy, stored in the order they
     * were registered.
     */
    protected List<T> listeners = new ArrayList<T>();
    
    
    /**
     * @param register Indicates if new listeners can be
     *                  (un)registered during execution 
     * @param acceptedClass Specifies the accepted class
     */
    @SuppressWarnings("hiding")
    public Serial(boolean register, Class<T> acceptedClass) {
        this.register = register;
        this.acceptedClass = acceptedClass;
    }
    
    @Override
    public Class<T> getAcceptedType() {
        return acceptedClass;
    }
    
    @Override
    public void register(T listener) {
        listeners.add(listener);
    }
    
    @Override
    public void unregister(T listener) {
        listeners.remove(listener);
    }
    
    @Override
    public boolean canRegisterDuringExecution() {
        return register;
    }

    @Override
    public boolean canUnregisterDuringExecution() {
        return register;
    }

}
