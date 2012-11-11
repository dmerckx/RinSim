package rinde.sim.core.simulation;



/**
 * This interfaces specifies the contract for a policy.
 * A policy is a collection accepting only  a certain class
 * of tick listeners.
 * Tick listeners accepted by this policy can be added
 * to this collection.
 * 
 * A policy can be instructed to perform the tick operation on
 * all its contained tick listeners. The implementation of a policy
 * is free to choose how this will be performed.
 * 
 * Typically some policies will perform this tick operation in a
 * parallel fashion, others will have a simple sequential operation.
 * 
 * 
 * @author dmerckx
 * @param <T> The type of tick listeners accepted by this policy.
 */
public interface TickPolicy<T extends TickListener<?>>{


    /**
     * Get the class of objects accepted by this policy rule.
     * 
     * @return The class of the type supported by this model.
     */
    Class<T> getAcceptedType();

    /**
     * Add a tickListener to this policy.
     * 
     * @param listener The tick listener to be added.
     */
    public void register(T listener); 

    /**
     * Remove a tickListener from this policy.
     * 
     * @param listener The tick listener to be removed
     */
    public void unregister(T listener);

    /**
     * Perform the tick operation of all the tick listeners
     * added to this policy.
     * 
     * @param interval The time interval of this execution.
     */
    public void performTicks(TimeInterval interval);


    /**
     * Returns whether or not new tick listeners can be added to the
     * {@link Simulator} during the execution of this policy.
     * 
     * @return True iff tick listeners can be added during this policy.
     */
    public boolean canRegisterDuringExecution();

    /**
     * Returns whether or not new tick listeners can be removed from the
     * {@link Simulator} during the execution of this policy.
     * 
     * @return True iff tick listeners can be removed during this policy.
     */
    public boolean canUnregisterDuringExecution();
}
