/**
 * 
 */
package rinde.sim.core.old.pdp;

/**
 * Abstract base class for depot concept: a stationary {@link Container_Old}.
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 */
public abstract class Depot_Old extends ContainerImpl {

    @Override
    public final PDPType getType() {
        return PDPType.DEPOT;
    }
}
