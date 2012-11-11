/**
 * 
 */
package rinde.sim.core.model.pdp;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newLinkedHashMap;
import static com.google.common.collect.Sets.newLinkedHashSet;
import static java.util.Collections.unmodifiableCollection;
import static java.util.Collections.unmodifiableSet;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import rinde.sim.core.graph.Point;
import rinde.sim.core.model.Model;
import rinde.sim.core.model.ModelProvider;
import rinde.sim.core.model.ModelReceiver;
import rinde.sim.core.model.pdp.twpolicy.LiberalPolicy;
import rinde.sim.core.model.pdp.twpolicy.TimeWindowPolicy;
import rinde.sim.core.model.road.RoadModel;
import rinde.sim.core.simulation.TimeInterval;
import rinde.sim.core.simulation.TimeLapse;
import rinde.sim.core.simulation.types.PrimaryTickListener;
import rinde.sim.event.Event;
import rinde.sim.event.EventAPI;
import rinde.sim.event.EventDispatcher;
import rinde.sim.util.CategoryMap;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;

/**
 * Assumptions of the model, any vehicle can pickup any (kind of) parcel (as
 * long as size constraints are met).
 * 
 * Currently supports three kinds of objects:
 * <ul>
 * <li> {@link Parcel}</li>
 * <li> {@link Vehicle}</li>
 * <li> {@link Depot}</li>
 * </ul>
 * 
 * A parcel must be in one of three locations: on a vehicle, in a depot or on a
 * road (roadmodel).
 * 
 * Variable pickup and delivery times are supported. Even when pickup time spans
 * multiple simulation ticks, the {@link PDPModel} ensures time consistency.
 * 
 * TODO write more about assumptions in model <br/>
 * TODO write about extensibility
 * 
 * 
 * 
 * {@link Parcel}s can be added on any node in the {@link RoadModel}.
 * {@link Depot}s have no real function in the current implementation.
 * 
 * 
 * 
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 */
public class PDPModel implements Model<PDPObject>, ModelReceiver, PrimaryTickListener {

    /**
     * {@link EventAPI} which allows adding and removing listeners to the model.
     */
    protected final EventAPI eventAPI;
    /**
     * The {@link EventDispatcher} used for generating events.
     */
    protected final EventDispatcher eventDispatcher;
    /**
     * Reference to the {@link RoadModel} on which the pdp objects are situated.
     */
    protected RoadModel roadModel;
    /**
     * Multimap for keeping references to the contents of {@link Container}s.
     */
    protected final Multimap<Container, Parcel> containerContents;
    /**
     * Map for keeping the size of the contents of {@link Container}s.
     */
    protected final Map<Container, Double> containerContentsSize;
    /**
     * Map for keeping the capacity of {@link Container}s.
     */
    protected final Map<Container, Double> containerCapacities;
    /**
     * Map that stores the state of {@link Vehicle}s.
     */
    protected final Map<Vehicle, VehicleState> vehicleState;
    /**
     * Map that stores the state of {@link Parcel}s.
     */
    protected final CategoryMap<ParcelState, Parcel> parcelState;
    /**
     * Map that stores any pending {@link Action}s of {@link Vehicle}s.
     */
    protected final Map<Vehicle, Action> pendingVehicleActions;

    protected final TimeWindowPolicy timeWindowPolicy;
    protected long currentTime;

    /**
     * The possible states a {@link Parcel} can be in.
     * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
     */
    public enum ParcelState {
        
        // TODO perhaps a LATE state could be added as well, indicating that a
        // parcel is late for pickup
        /**
         * State that indicates that the {@link Parcel} is not yet available for
         * pickup but that it will be in the (near) future.
         */
        ANNOUNCED,
        /**
         * State that indicates that the {@link Parcel} is available for pickup.
         */
        AVAILABLE,
        /**
         * State that indicates that the {@link Parcel} is in the process of
         * being picked up.
         */
        PICKING_UP,
        /**
         * State that indicates that the {@link Parcel} is currently in the
         * cargo of a {@link Vehicle}.
         */
        IN_CARGO,
        /**
         * State that indicates that the {@link Parcel} is in the process of
         * being delivered.
         */
        DELIVERING,
        /**
         * State that indicates that the {@link Parcel} has been delivered.
         */
        DELIVERED
    }

    /**
     * The possible states a {@link Vehicle} can be in.
     * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
     */
    public enum VehicleState {
        /**
         * The 'normal' state, indicating that a {@link Vehicle} is neither in
         * {@link #PICKING_UP} nor in {@link #DELIVERING} state.
         */
        IDLE,
        /**
         * State that indicates that the {@link Vehicle} is currently picking up
         * a {@link Parcel}.
         */
        PICKING_UP,
        /**
         * State that indicates that the {@link Vehicle} is currently delivering
         * a {@link Parcel}.
         */
        DELIVERING
    }

    /**
     * The possible {@link Event} types that the {@link PDPModel} dispatches.
     * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
     */
    public enum PDPModelEventType {
        /**
         * Indicates the start of a pickup of a {@link Parcel} by a
         * {@link Vehicle}.
         */
        START_PICKUP,
        /**
         * Indicates the end of a pickup of a {@link Parcel} by a
         * {@link Vehicle}.
         */
        END_PICKUP,
        /**
         * Indicates the start of a delivery of a {@link Parcel} by a
         * {@link Vehicle}.
         */
        START_DELIVERY,
        /**
         * Indicates the start of a delivery of a {@link Parcel} by a
         * {@link Vehicle}.
         */
        END_DELIVERY,

        NEW_PARCEL
    }

    public PDPModel() {
        this(new LiberalPolicy());
    }

    /**
     * Initializes the PDPModel.
     * @param rm The {@link RoadModel} which is associated to this model.
     */
    public PDPModel(TimeWindowPolicy twp) {
        timeWindowPolicy = twp;
        containerContents = LinkedHashMultimap.create();
        containerContentsSize = newLinkedHashMap();
        containerCapacities = newLinkedHashMap();
        pendingVehicleActions = newLinkedHashMap();
        vehicleState = newLinkedHashMap();
        parcelState = CategoryMap.create();

        eventDispatcher = new EventDispatcher(PDPModelEventType.values());
        eventAPI = eventDispatcher.getEventAPI();
    }

    /**
     * Returns an unmodifiable view on the contents of the specified container.
     * @param container The container to inspect.
     * @return An unmodifiable collection.
     */
    public Collection<Parcel> getContents(Container container) {
        checkArgument(containerCapacities.containsKey(container));
        return unmodifiableCollection(containerContents.get(container));
    }

    /**
     * Returns the size of the contents of the specified container.
     * @param container The container to inspect.
     * @return A <code>double</code> indicating the size of the contents.
     */
    public double getContentsSize(Container container) {
        return containerContentsSize.get(container);
    }

    public double getContainerCapacity(Container container) {
        return containerCapacities.get(container);
    }

    /**
     * Attempts to pickup the specified {@link Parcel} into the specified
     * {@link Vehicle}. Preconditions:
     * <ul>
     * <li>{@link Vehicle} must be on {@link RoadModel}.</li>
     * <li>{@link Vehicle} must be registered in {@link PDPModel}.</li>
     * <li>{@link Vehicle} must be in {@link VehicleState#IDLE} state.</li>
     * <li>{@link Parcel} must be on {@link RoadModel}.</li>
     * <li>{@link Parcel} must be registered in {@link PDPModel}.</li>
     * <li>{@link Parcel} must be in {@link ParcelState#AVAILABLE} state.</li>
     * <li>{@link Vehicle} and {@link Parcel} must be at same position in
     * {@link RoadModel}.</li>
     * <li>{@link Parcel} must fit in {@link Vehicle}.</li>
     * </ul>
     * If any of the preconditions is not met this method throws an
     * {@link IllegalArgumentException}.
     * <p>
     * When all preconditions are met, the pickup action is started indicated by
     * the dispatching of an {@link Event} with type
     * {@link PDPModelEventType#START_PICKUP}. In case the specified
     * {@link TimeLapse} is not big enough to complete the pickup immediately
     * the action will be continued next tick. When the pickup action is
     * completed an {@link Event} with type {@link PDPModelEventType#END_PICKUP}
     * is dispatched. When done, the {@link Parcel} will be contained by the
     * {@link Vehicle}.
     * @param vehicle The {@link Vehicle} involved in pickup.
     * @param parcel The {@link Parcel} to pick up.
     * @param time The {@link TimeLapse} that is available for the action.
     */
    public void pickup(Vehicle vehicle, Parcel parcel, TimeLapse time) {
        /* 1 */checkArgument(roadModel.containsObject(vehicle), "vehicle does not exist in RoadModel");
        /* 2 */checkArgument(roadModel.containsObject(parcel), "parcel does not exist in RoadModel");
        /* 3 */checkArgument(parcelState.getKeys(parcel) == ParcelState.AVAILABLE, "parcel must be registered and must be available");
        /* 4 */checkArgument(vehicleState.get(vehicle) == VehicleState.IDLE, "vehicle must be registered and must be available");
        /* 5 */checkArgument(roadModel.equalPosition(vehicle, parcel), "vehicle must be at the same location as the parcel it wishes to pickup");
        final double newSize = containerContentsSize.get(vehicle)
                + parcel.getMagnitude();
        /* 6 */checkArgument(newSize <= containerCapacities.get(vehicle), "parcel does not fit in vehicle");

        checkArgument(timeWindowPolicy.canPickup(parcel.getPickupTimeWindow(), time
                .getTime(), parcel.getPickupDuration()), "parcel pickup is not allowed according to the time window policy: "
                + timeWindowPolicy);

        checkArgument(parcel.canBePickedUp(vehicle, time.getTime()), "the parcel does not allow pickup now");

        eventDispatcher.dispatchEvent(new PDPModelEvent(
                PDPModelEventType.START_PICKUP, this, time.getTime(), parcel));

        // remove the parcel such that it can no longer be attempted to be
        // picked up by anyone else
        roadModel.removeObject(parcel);
        // in this case we know we cannot finish this action with the available
        // time. We must continue in the next tick.
        if (time.getTimeLeft() < parcel.getPickupDuration()) {
            vehicleState.put(vehicle, VehicleState.PICKING_UP);
            parcelState.put(ParcelState.PICKING_UP, parcel);

            pendingVehicleActions.put(vehicle, new PickupAction(this, vehicle,
                    parcel, parcel.getPickupDuration() - time.getTimeLeft()));
            time.consumeAll();
        } else {
            time.consume(parcel.getPickupDuration());
            doPickup(vehicle, parcel, time.getTime());
        }
    }

    /**
     * Actual pickup, updates the {@link Vehicle} contents.
     * @param vehicle The {@link Vehicle} that performs the pickup.
     * @param parcel The {@link Parcel} that is picked up.
     * @see #pickup(Vehicle, Parcel, TimeLapse)
     */
    protected void doPickup(Vehicle vehicle, Parcel parcel, long time) {
        containerContents.put(vehicle, parcel);
        containerContentsSize.put(vehicle, containerContentsSize.get(vehicle)
                + parcel.getMagnitude());

        parcelState.put(ParcelState.IN_CARGO, parcel);
        eventDispatcher.dispatchEvent(new PDPModelEvent(
                PDPModelEventType.END_PICKUP, this, time, parcel));
    }

    /**
     * The specified {@link Vehicle} attempts to deliver the {@link Parcel} at
     * its current location. Preconditions:
     * <ul>
     * <li>{@link Vehicle} must exist in {@link RoadModel}.</li>
     * <li>{@link Vehicle} must be in {@link VehicleState#IDLE} state.</li>
     * <li>{@link Vehicle} must contain the specified {@link Parcel}.</li>
     * <li>{@link Vehicle} must be at the position indicated by
     * {@link Parcel#getDestination()}.</li>
     * </ul>
     * If any of the preconditions is not met this method throws an
     * {@link IllegalArgumentException}.
     * <p>
     * When all preconditions are met the actual delivery is started, this is
     * indicated by the dispatching of an {@link Event} with
     * {@link PDPModelEventType#START_DELIVERY} type. If there is not enough
     * time in the specified {@link TimeLapse} to complete the delivery at once,
     * the action will be completed in the next tick. When the delivery is
     * completed an {@link Event} with type
     * {@link PDPModelEventType#END_DELIVERY} is dispatched. As a result the
     * {@link Vehicle} no longer contains the {@link Parcel} and the
     * {@link Parcel} is NOT added to the {@link RoadModel} again.
     * @param vehicle The {@link Vehicle} that wishes to deliver a
     *            {@link Parcel}.
     * @param parcel The {@link Parcel} that is to be delivered.
     * @param time The {@link TimeLapse} that is available for delivery.
     */
    public void deliver(Vehicle vehicle, Parcel parcel, TimeLapse time) {
        /* 1 */checkArgument(roadModel.containsObject(vehicle), "vehicle does not exist in RoadModel");
        /* 2 */checkArgument(vehicleState.get(vehicle)
                .equals(VehicleState.IDLE), "vehicle must be idle but is: "
                + vehicleState.get(vehicle));
        /* 3 */checkArgument(containerContents.get(vehicle).contains(parcel), "vehicle does not contain parcel");
        /* 4 */checkArgument(parcel.getDestination()
                .equals(roadModel.getPosition(vehicle)), "parcel must be delivered at its destination, vehicle should move there first");

        checkArgument(timeWindowPolicy.canDeliver(parcel
                .getDeliveryTimeWindow(), time.getTime(), parcel
                .getDeliveryDuration()), "parcel delivery is not allowed according to the time window policy: "
                + timeWindowPolicy);

        checkArgument(parcel.canBeDelivered(vehicle, time.getTime()), "the parcel does not allow a delivery now");

        eventDispatcher
                .dispatchEvent(new PDPModelEvent(
                        PDPModelEventType.START_DELIVERY, this, time.getTime(),
                        parcel));
        if (time.getTimeLeft() < parcel.getDeliveryDuration()) {
            vehicleState.put(vehicle, VehicleState.DELIVERING);
            parcelState.put(ParcelState.DELIVERING, parcel);
            pendingVehicleActions.put(vehicle, new DeliverAction(this, vehicle,
                    parcel, parcel.getDeliveryDuration() - time.getTimeLeft()));
            time.consumeAll();
        } else {
            time.consume(parcel.getDeliveryDuration());
            doDeliver(vehicle, parcel, time.getTime());
        }
    }

    /**
     * The actual delivery of the specified {@link Parcel} by the specified
     * {@link Vehicle}.
     * @param vehicle The {@link Vehicle} that performs the delivery.
     * @param parcel The {@link Parcel} that is delivered.
     */
    protected void doDeliver(Vehicle vehicle, Parcel parcel, long time) {
        containerContents.remove(vehicle, parcel);
        containerContentsSize.put(vehicle, containerContentsSize.get(vehicle)
                - parcel.getMagnitude());

        parcelState.put(ParcelState.DELIVERED, parcel);
        eventDispatcher.dispatchEvent(new PDPModelEvent(
                PDPModelEventType.END_DELIVERY, this, time, parcel));
    }

    /**
     * This method is intended for {@link Parcel}s that wish to add themselves
     * to either a {@link Vehicle} or a {@link Depot}.
     * @param container The {@link Container} to which the specified
     *            {@link Parcel} is added.
     * @param parcel The {@link Parcel} that is added.
     */
    public void addParcelIn(Container container, Parcel parcel) {
        /* 1 */checkArgument(!roadModel.containsObject(parcel), "this parcel is already added to the roadmodel");
        /* 2 */checkArgument(parcelState.getKeys(parcel) == ParcelState.AVAILABLE, "parcel must be registered and in AVAILABLE state, current state: "
                + parcelState.getKeys(parcel));
        /* 3 */checkArgument(containerCapacities.containsKey(container), "the parcel container is not registered");
        /* 4 */checkArgument(roadModel.containsObject(container), "the parcel container is not on the roadmodel");
        final double newSize = containerContentsSize.get(container)
                + parcel.getMagnitude();
        /* 5 */checkArgument(newSize <= containerCapacities.get(container), "parcel does not fit in container. Capacity is "
                + containerCapacities.get(container)
                + ", current content size is "
                + containerContentsSize.get(container)
                + ", new parcel size is " + parcel.getMagnitude());

        containerContents.put(container, parcel);
        containerContentsSize.put(container, newSize);
        parcelState.put(ParcelState.IN_CARGO, parcel);
    }

    /**
     * @return An unmodifiable view on the the parcels which are in
     *         <code>AVAILABLE</code> state. Note that parcels which are
     *         available are not neccesarily already at a position.
     */
    // TODO convert to more generic version with state as argument
    public Collection<Parcel> getAvailableParcels() {
        return parcelState.get(ParcelState.AVAILABLE);
    }

    public Collection<Parcel> getParcels(ParcelState state) {
        return parcelState.get(state);
    }

    public Collection<Parcel> getParcels(ParcelState... states) {
        final Collection<Parcel> parcels = newLinkedHashSet();
        for (final ParcelState s : states) {
            parcels.addAll(parcelState.get(s));
        }
        return unmodifiableCollection(parcels);
    }

    @SuppressWarnings("unchecked")
    // public <T extends Parcel> Set<T> getParcelsWithStateAndType(
    // final ParcelState state, final Class<T> clazz) {
    //
    // return unmodifiableSet((Set<T>) filterEntries(parcelState, new
    // Predicate<Map.Entry<Parcel, ParcelState>>() {
    // @Override
    // public boolean apply(Map.Entry<Parcel, ParcelState> input) {
    // return input.getValue() == state
    // && clazz.isInstance(input.getKey());
    // }
    // }).keySet());
    // }
    public Set<Vehicle> getVehicles() {
        return unmodifiableSet(vehicleState.keySet());
    }

    /**
     * @param parcel The {@link Parcel} for which the state is checked.
     * @return The {@link ParcelState} of the specified {@link Parcel}.
     */
    public ParcelState getParcelState(Parcel parcel) {
        return parcelState.getKeys(parcel);
    }

    /**
     * @param vehicle The {@link Vehicle} for which the state is checked.
     * @return The {@link VehicleState} of the specified {@link Vehicle}.
     */
    public VehicleState getVehicleState(Vehicle vehicle) {
        checkArgument(vehicleState.containsKey(vehicle), "vehicle must be registered");
        return vehicleState.get(vehicle);
    }

    public VehicleParcelActionInfo getVehicleActionInfo(Vehicle vehicle) {
        checkArgument(vehicleState.get(vehicle) == VehicleState.DELIVERING
                || vehicleState.get(vehicle) == VehicleState.PICKING_UP, "the vehicle must be in either DELIVERING or PICKING_UP state");
        return (VehicleParcelActionInfo) pendingVehicleActions.get(vehicle);
    }

    public Point getPosition(PDPObject obj) {
        return roadModel.getPosition(obj);
    }

    @Override
    public void register(PDPObject element) {
        if (element.getType() == PDPType.PARCEL) {
            checkArgument(!parcelState.containsValue(element));
            final Parcel p = (Parcel) element;
            parcelState
                    .put(currentTime < p.getPickupTimeWindow().begin ? ParcelState.ANNOUNCED
                            : ParcelState.AVAILABLE, (Parcel) element);
            eventDispatcher.dispatchEvent(new PDPModelEvent(
                    PDPModelEventType.NEW_PARCEL, this, currentTime, p));
        } else { /*
                  * if (element.getType() == PDPType.VEHICLE ||
                  * element.getType() == PDPType.DEPOT)
                  */
            final Container container = (Container) element;
            checkArgument(!containerCapacities.containsKey(container));
            containerCapacities.put(container, container.getCapacity());
            containerContentsSize.put(container, 0d);

            if (element.getType() == PDPType.VEHICLE) {
                vehicleState.put((Vehicle) element, VehicleState.IDLE);
            }
        }
        element.initPDPObject(this);
    }

    @Override
    public void unregister(PDPObject element) {
        // TODO implement
        throw new UnsupportedOperationException();
    }

    @Override
    public Class<PDPObject> getSupportedType() {
        return PDPObject.class;

    }

    public EventAPI getEventAPI() {
        return eventAPI;
    }

    /**
     * Inspects the contents of the specified {@link Container} for existence of
     * the specified {@link Parcel} object.
     * @param container The container which is inspected.
     * @param parcel The parcel which we are checking.
     * @return <code>true</code> if the {@link Parcel} is contained in the
     *         {@link Container}, <code>false</code> otherwise.
     */
    public boolean containerContains(Container container, Parcel parcel) {
        return containerContents.containsEntry(container, parcel);
    }

    /**
     * This method should be called by {@link Vehicle}s that need to finish a
     * previously started {@link Action}. By calling this method before
     * executing other actions, time consistency is enforced since any pending
     * actions will consume time first. It is possible that after this call is
     * completed, there is no time left for other actions. When the specified
     * {@link Vehicle} has no pending {@link Action}s nothing will happen.
     * @param vehicle {@link Vehicle}
     * @param time {@link TimeLapse} that is available for performing the
     *            actions.
     */
    protected void continuePreviousActions(Vehicle vehicle, TimeLapse time) {
        if (pendingVehicleActions.containsKey(vehicle)) {
            final Action action = pendingVehicleActions.get(vehicle);
            action.perform(time);
            if (action.isDone()) {
                pendingVehicleActions.remove(vehicle);
                checkState(!pendingVehicleActions.containsKey(vehicle));
                checkState(vehicleState.get(vehicle) == VehicleState.IDLE);
            }

        }
    }

    @Override
    public void tick(TimeInterval timeLapse) {
        currentTime = timeLapse.getStartTime();
        final Collection<Parcel> parcels = parcelState
                .get(ParcelState.ANNOUNCED);
        final List<Parcel> newAvailables = newArrayList();
        for (final Parcel p : parcels) {
            if (timeLapse.getStartTime() >= p.getPickupTimeWindow().begin) {
                newAvailables.add(p);
            }
        }
        for (final Parcel p : newAvailables) {
            parcelState.put(ParcelState.AVAILABLE, p);
        }
    }
    
    public TimeWindowPolicy getTimeWindowPolicy() {
        return timeWindowPolicy;
    }

    @Override
    public void registerModelProvider(ModelProvider mp) {
        roadModel = mp.getModel(RoadModel.class);
    }

    public class PDPModelEvent extends Event {

        public final PDPModel pdpModel;
        public final long time;
        public final Parcel parcel;

        public PDPModelEvent(PDPModelEventType type, PDPModel model, long t,
                Parcel p) {
            super(type, model);
            pdpModel = model;
            time = t;
            parcel = p;
        }

    }

    /**
     * Represents an action that takes time. This is used for actions that can
     * not be done at once (since there is not enough time available), using
     * this interface actions can be performed in steps.
     * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
     */
    protected interface Action {
        /**
         * Performs the action using the specified amount of time.
         * @param time
         */
        void perform(TimeLapse time);

        /**
         * @return <code>true</code> when this action is completed,
         *         <code>false</code> otherwise.
         */
        boolean isDone();
    }

    public interface VehicleParcelActionInfo {
        long timeNeeded();

        Vehicle getVehicle();

        Parcel getParcel();
    }

    abstract class VehicleParcelAction implements Action,
            VehicleParcelActionInfo {
        protected final PDPModel modelRef;
        protected final Vehicle vehicle;
        protected final Parcel parcel;
        protected long timeNeeded;

        public VehicleParcelAction(PDPModel model, Vehicle v, Parcel p,
                long pTimeNeeded) {
            modelRef = model;
            vehicle = v;
            parcel = p;
            timeNeeded = pTimeNeeded;
        }

        @Override
        public void perform(TimeLapse time) {
            // there is enough time to finish action
            if (time.getTimeLeft() >= timeNeeded) {
                time.consume(timeNeeded);
                timeNeeded = 0;
                finish(time);
            } else { // there is not enough time to finish action in this step
                timeNeeded -= time.getTimeLeft();
                time.consumeAll();
            }
        }

        abstract protected void finish(TimeLapse time);

        @Override
        public boolean isDone() {
            return timeNeeded == 0;
        }

        @Override
        public long timeNeeded() {
            return timeNeeded;
        }

        @Override
        public Parcel getParcel() {
            return parcel;
        }

        @Override
        public Vehicle getVehicle() {
            return vehicle;
        }
    }

    class PickupAction extends VehicleParcelAction {

        public PickupAction(PDPModel model, Vehicle v, Parcel p,
                long pTimeNeeded) {
            super(model, v, p, pTimeNeeded);
        }

        @Override
        public void finish(TimeLapse time) {
            modelRef.vehicleState.put(vehicle, VehicleState.IDLE);
            modelRef.doPickup(vehicle, parcel, time.getTime());

        }
    }

    class DeliverAction extends VehicleParcelAction {

        public DeliverAction(PDPModel model, Vehicle v, Parcel p,
                long pTimeNeeded) {
            super(model, v, p, pTimeNeeded);
        }

        @Override
        public void finish(TimeLapse time) {
            modelRef.vehicleState.put(vehicle, VehicleState.IDLE);
            modelRef.doDeliver(vehicle, parcel, time.getTime());
        }
    }
}
