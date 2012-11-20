/**
 * 
 */
package rinde.sim.core.old.pdp;

/**
 * Implementors of this interface can contain 'things', typically {@link Parcel_Old}
 * objects. This interface is typically not used directly, two often used
 * implementations are {@link Vehicle_Old} and {@link Depot_Old}.
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 */
public interface Container_Old extends PDPObject {

    /**
     * The returned value is treated as a constant (i.e. it is read only once).
     * @return The maximum capacity of the container.
     */
    double getCapacity();
}
