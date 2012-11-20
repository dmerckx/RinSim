/**
 * 
 */
package rinde.sim.core.old.pdp;

import static com.google.common.base.Preconditions.checkState;

/**
 * Default implementation of the {@link Container_Old} interface.
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 */
public abstract class ContainerImpl extends PDPObjectImpl implements Container_Old {

    private double capacity;

    /**
     * Sets the capacity. This must be set before the object is registered in
     * its model.
     * @param pCapacity The capacity to use.
     */
    protected final void setCapacity(double pCapacity) {
        checkState(!isRegistered(), "capacity must be set before object is registered, it can not be changed afterwards.");
        capacity = pCapacity;
    }

    @Override
    public final double getCapacity() {
        return capacity;
    }
}
