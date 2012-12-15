package rinde.sim.core.model.pdp.apis;

import java.util.List;

import rinde.sim.core.model.Data;
import rinde.sim.core.model.User;
import rinde.sim.core.model.pdp.users.Container;
import rinde.sim.core.model.pdp.users.Parcel;
import rinde.sim.core.model.road.RoadModel;
import rinde.sim.core.model.road.users.RoadUser;
import rinde.sim.core.simulation.TimeLapse;

/**
 * This is the api designed for any kind of {@link Container}.
 * The api must be initialized with a certain capacity which 
 * limits the total load of packages a container can carry.
 * 
 * Since every {@link Container} is also a {@link RoadUser} they
 * have a location on the {@link RoadModel}.
 * This api allows {@link Container}s to pickup and deliver
 * {@link Parcel}s to other {@link Container}s, {@link DeliveryPoint}s
 * and {@link PickupPoint}s.
 * 
 * 
 * @author dmerckx
 *
 * @param <P> The type of parcels contained.
 */
public interface ContainerAPI extends User<Data>{
    
    
    /**
     * Returns all the parcels currently contained.
     * @return The list of contained parcels.
     */
    public List<Parcel> getLoad();
    
    
    /**
     * Returns the amount of capacity left to store more load.
     * @return The amount of capacity left.
     */
    public double getCapacityLeft();
    
    /**
     * Try to pickup the first available parcel on the current location.
     * This will first locate all {@link PickupPoint}s on this location 
     * and then try to load up the first available parcel.
     * 
     * Note that if a parcel is available the {@link Container} state
     * will change to {@link ContainerState} <code>PICKING_UP</code> and no
     * time will be available until the container is done loading. 
     *  
     * @param lapse The time to perform the pickup if available
     * @return Returns the parcel that is being picked up or null if
     * none were available
     */
    public Parcel tryPickup(TimeLapse lapse);
    
    /**
     * Try to pickup the given parcel on the current location, from
     * any of the available {@link PickupPoint}s or {@link Container}s.
     * 
     * Note that the parcel is available the {@link Container} state
     * will change to {@link ContainerState} <code>PICKING_UP</code> and no
     * time will be available until the container is done loading. 
     * 
     * @param lapse The time to perform the pickup if available
     * @param parcel The parcel to be picked up
     * @return True iff the given parcel is (being) picked up
     */
    public boolean tryPickupOf(TimeLapse lapse, Parcel parcel);
    
    public void acceptAll(TimeLapse lapse);
    
    public void accept(TimeLapse lapse, List<Parcel> parcels);
    
    /**
     * Advertises all parcels contained by this object at this location.
     * Advertised parcels can be picked up by other containers.
     * This will consume whatever time is left in the given time lapse.
     *
     * @param lapse The time to perform this action (will be fully consumed)
     */
    public void advertiseAll(TimeLapse lapse);
    
    /**
     * Advertises all the provided parcels that are contained by this object.
     * Advertised parcels can be picked up by other containers.
     * This will consume whatever time is left in the given time lapse.
     * 
     * @param lapse The time to perform this action (will be fully consumed)
     * @param parcels The parcels to advertise
     */
    public void advertise(TimeLapse lapse, List<Parcel> parcels);
    
    
    /**
     * Returns the current state of this container.
     * @return The current state
     */
    public ContainerState getState();
    
    /**
     * The possible states a {@link Container} can be in.
     */
    public enum ContainerState {
        /**
         * The 'normal' state, nothing is going on at this point.
         */
        AVAILABLE,
        /**
         * This state indicates that the container is waiting for other parcel(s)
         * to be deposited into this container.
         */
        ACCEPTING,
        /**
         * This state indicates that a (subset) of the parcels contained by this 
         * container are available to be picked up by others.
         */
        ADVERTISING,
        /**
         * 
         */
        ACCEPTING_ADVERTISING,
        /**
         * This State indicates that this container is currently picking up
         * a {@link Parcel} from another {@link Container} or a {@link PickupPoint}.
         */
        PICKING_UP,
        /**
         * This State indicates that the {@link Container} is currently delivering
         * a {@link Parcel} to another {@link Container} or a {@link DeliveryPoint}.
         */
        DELIVERING
    }
}
