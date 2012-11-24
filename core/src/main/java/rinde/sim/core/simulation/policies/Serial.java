package rinde.sim.core.simulation.policies;

import java.util.ArrayList;
import java.util.List;

import rinde.sim.core.simulation.TickPolicy;
import rinde.sim.core.simulation.TimeInterval;

/**
 * A serial implementation of a general policy. Listeners are stored
 * in an list and are executed in the same order they were added to
 * this policy.
 * 
 * @author dmerckx
 * @param <T> The type of tick listeners accepted by this policy.
 */
public abstract class Serial<T> implements TickPolicy<T>{

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
    public Serial(boolean register) {
        this.register = register;
    }
    
    public abstract void doTick(T obj, TimeInterval interval);
    
    @Override
    public void register(T listener) {
        listeners.add(listener);
    }
    
    @Override
    public void unregister(T listener) {
        listeners.remove(listener);
    }

    @Override
    public void performTicks(TimeInterval interval) {
        for(T listener:listeners){
            doTick(listener, interval);
        }
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
