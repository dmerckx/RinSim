package rinde.sim.core.model.pdp;

import java.util.List;
import java.util.Set;

import rinde.sim.core.model.Data;
import rinde.sim.core.model.Model;
import rinde.sim.core.model.User;
import rinde.sim.core.model.pdp.apis.ContainerAPI;
import rinde.sim.core.model.pdp.apis.ContainerGuard;
import rinde.sim.core.model.pdp.apis.DeliveryAPI;
import rinde.sim.core.model.pdp.apis.DeliveryGuard;
import rinde.sim.core.model.pdp.apis.PickupAPI;
import rinde.sim.core.model.pdp.apis.PickupGuard;
import rinde.sim.core.model.pdp.apis.TruckGuard;
import rinde.sim.core.model.pdp.twpolicy.TimeWindowPolicy;
import rinde.sim.core.model.pdp.users.Container;
import rinde.sim.core.model.pdp.users.ContainerData;
import rinde.sim.core.model.pdp.users.DeliveryPoint;
import rinde.sim.core.model.pdp.users.DeliveryPointData;
import rinde.sim.core.model.pdp.users.PdpUser;
import rinde.sim.core.model.pdp.users.PickupPoint;
import rinde.sim.core.model.pdp.users.PickupPointData;
import rinde.sim.core.model.pdp.users.Truck;
import rinde.sim.core.model.pdp.users.TruckData;
import rinde.sim.core.simulation.TimeInterval;
import rinde.sim.core.simulation.UserInit;
import rinde.sim.core.simulation.policies.InteractionRules;
import rinde.sim.core.simulation.time.TimeLapseHandle;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

@SuppressWarnings("rawtypes")
public class PdpModel implements Model<Data, PdpUser<?>>{
    public final TimeWindowPolicy twp;
    public final double range;

    //TODO: order these events to present them deterministically
    private final PdpObserver observer;
    private Set<PickupPoint<?>> pickupEvents = Sets.newLinkedHashSet();
    private Set<DeliveryPoint<?>> deliveryEvents = Sets.newLinkedHashSet();
    
    public PdpModel(TimeWindowPolicy twp, double range) {
        this(twp, range, null);
    }
    
    public PdpModel(TimeWindowPolicy twp, double range, PdpObserver observer) {
        this.twp = twp;
        this.observer = observer;
        this.range = range;
    }
    
    public TimeWindowPolicy getPolicy(){
        return twp;
    }
    
    // ----- NOTIFICATIONS ----- //
    
    public synchronized void notifyParcelPickup(PickupPoint<?> p){
        pickupEvents.add(p);
    }
    
    public synchronized void notifyParcelDelivery(DeliveryPoint<?> d){
        deliveryEvents.add(d);
    }
    
    // ------ MODEL ------ //

    @Override
    public List<UserInit<?>> register(PdpUser<?> user, Data data, TimeLapseHandle handle) {
        assert user != null;
        assert data != null;
        
        List<UserInit<?>> result = Lists.newArrayList();
        
        User<Data> guard = null;
        
        if(user instanceof Container<?>){
            guard = new ContainerGuard((Container) user, (ContainerData) data, this, handle);
            ((Container) user).setContainerAPI((ContainerAPI) guard);
            
            
            if(user instanceof Truck<?>){
                TruckGuard guard2 = new TruckGuard((Truck<?>) user, (TruckData) data, this);
                ((Truck) user).setTruckAPI(guard2);
            }
        }
        else if(user instanceof PickupPoint){
            guard = new PickupGuard((PickupPoint<?>) user, (PickupPointData) data, this, handle);
            ((PickupPoint) user).setPickupAPI((PickupAPI) guard);
        }
        else if(user instanceof DeliveryPoint){
            guard = new DeliveryGuard((DeliveryPoint<?>) user, (DeliveryPointData) data, this, handle);
            ((DeliveryPoint) user).setDeliveryAPI((DeliveryAPI) guard);
        }
        else {
            throw new IllegalArgumentException("The user " + user + " has not a valid type known by this pdp model");
        }
        
        result.add(UserInit.create(guard));
        
        return result;
    }


    @Override
    public void unregister(PdpUser<?> user) {
        assert user != null;
    }

    @Override
    public Class<PdpUser<?>> getSupportedType() {
        return (Class) PdpUser.class;
    }

    @Override
    public void tick(TimeInterval time) {
        if(observer != null){
            for(PickupPoint<?> p:pickupEvents){
               observer.packagePickedUp(p); 
            }
            for(DeliveryPoint<?> d:deliveryEvents){
                observer.packageDelivered(d);
            }
        }
        pickupEvents.clear();
        deliveryEvents.clear();
    }
    
    @Override
    public void init(long seed, InteractionRules rules, TimeInterval masterTime) {
        
    }
}
