package rinde.sim.core.model.pdp;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import rinde.sim.core.model.Data;
import rinde.sim.core.model.Model;
import rinde.sim.core.model.User;
import rinde.sim.core.model.pdp.apis.ContainerGuard;
import rinde.sim.core.model.pdp.apis.DeliveryGuard;
import rinde.sim.core.model.pdp.apis.PickupGuard;
import rinde.sim.core.model.pdp.apis.TruckGuard;
import rinde.sim.core.model.pdp.twpolicy.TimeWindowPolicy;
import rinde.sim.core.model.pdp.users.Container;
import rinde.sim.core.model.pdp.users.ContainerData;
import rinde.sim.core.model.pdp.users.DeliveryPoint;
import rinde.sim.core.model.pdp.users.DeliveryPointData;
import rinde.sim.core.model.pdp.users.Depot;
import rinde.sim.core.model.pdp.users.PdpUser;
import rinde.sim.core.model.pdp.users.PickupPoint;
import rinde.sim.core.model.pdp.users.PickupPointData;
import rinde.sim.core.model.pdp.users.Truck;
import rinde.sim.core.model.pdp.users.TruckData;
import rinde.sim.core.simulation.TimeInterval;
import rinde.sim.core.simulation.UserInit;
import rinde.sim.core.simulation.time.TimeLapseHandle;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

@SuppressWarnings("rawtypes")
public class PdpModel implements Model<Data, PdpUser<?>>, PdpAPI{

    private TimeInterval time;
    private final TimeWindowPolicy twp;
    
    
    private PdpObserver observer;
    //TODO: order these events to present them deterministically
    private Set<PickupPoint<?>> pickupEvents = Sets.newLinkedHashSet();
    private Set<DeliveryPoint<?>> deliveryEvents = Sets.newLinkedHashSet();
    
    private Map<Depot<?>, User<?>> depots = Maps.newLinkedHashMap();
    private Map<Truck<?>, User<?>> trucks = Maps.newLinkedHashMap();
    private Map<PickupPoint<?>, User<?>> pickups = Maps.newLinkedHashMap();
    private Map<DeliveryPoint<?>, User<?>> deliveries = Maps.newLinkedHashMap();
    
    public PdpModel(TimeWindowPolicy twp) {
        this(twp, null);
    }
    
    public PdpModel(TimeWindowPolicy twp, PdpObserver observer) {
        this.twp = twp;
        this.observer = observer;
    }
    
    public TimeWindowPolicy getPolicy(){
        return twp;
    }
    
    public TimeInterval getTime(){
        return time;
    }
    
    // ----- QUERIES ----- //
    
    @Override
    public Iterator<Truck<?>> queryTrucks(){
        return getSafeIterator(trucks.keySet());
    }
    
    @Override
    public Iterator<Depot<?>> queryDepots(){
        return getSafeIterator(depots.keySet());
    }
    
    @Override
    public Iterator<PickupPoint<?>> queryPickups(){
        return getSafeIterator(pickups.keySet());
    }
    
    @Override
    public Iterator<DeliveryPoint<?>> queryDeliveries(){
        return getSafeIterator(deliveries.keySet());
    }
    
    @Override
    public Iterator<Container<?>> queryContainers(){
        final Iterator<Truck<?>> it1 = trucks.keySet().iterator();
        final Iterator<Depot<?>> it2 = depots.keySet().iterator();
        
        return new Iterator<Container<?>>() {
            @Override
            public boolean hasNext() {
                return it1.hasNext() || it2.hasNext();
            }

            @Override
            public Container<?> next() {
                return it1.hasNext()? it1.next() : it2.next();
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }};
    }
    
    private <T> Iterator<T> getSafeIterator(Collection<T> coll){
        final Iterator<T> it = coll.iterator();
        
        return new Iterator<T>() {
            @Override
            public boolean hasNext() {
                return it.hasNext();
            }
            
            @Override
            public T next() {
                return it.next();
            }
            
            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }
    
    // ----- NOTIFICATIONS ----- //
    
    public void notifyParcelPickup(PickupPoint<?> p){
        pickupEvents.add(p);
    }
    
    public void notifyParcelDelivery(DeliveryPoint<?> d){
        deliveryEvents.add(d);
    }
    
    // ------ MODEL ------ //

    @Override
    public List<UserInit<?>> register(PdpUser<?> user, Data data, TimeLapseHandle handle) {
        assert user != null;
        assert data != null;
        
        List<UserInit<?>> result = Lists.newArrayList();
        
        if(user instanceof Container<?>){
            ContainerGuard guard = new ContainerGuard((Container) user, (ContainerData) data, this, handle);
            ((Container) user).setContainerAPI(guard);
            
            result.add(UserInit.create(guard));
            
            if(user instanceof Truck<?>){
                TruckGuard guard2 = new TruckGuard((Truck<?>) user, (TruckData) data, this);
                ((Truck) user).setTruckAPI(guard2);
                trucks.put((Truck<?>) user, guard);
            }
            else if(user instanceof Depot<?>){
                depots.put((Depot<?>) user, guard);
            }
        }
        else if(user instanceof PickupPoint){
            PickupGuard guard = new PickupGuard((PickupPoint<?>) user, (PickupPointData) data, this, handle);
            ((PickupPoint) user).setPickupAPI(guard);
            
            result.add(UserInit.create(guard));
            pickups.put((PickupPoint<?>) user, guard);
        }
        else if(user instanceof DeliveryPoint){
            DeliveryGuard guard = new DeliveryGuard((DeliveryPoint<?>) user, (DeliveryPointData) data, this, handle);
            ((DeliveryPoint) user).setDeliveryAPI(guard);
            
            result.add(UserInit.create(guard));
            deliveries.put((DeliveryPoint<?>)user, guard);
        }
        else {
            throw new IllegalArgumentException("The user " + user + " has not a valid type known by this pdp model");
        }
        
        return result;
    }


    @Override
    public List<User<?>> unregister(PdpUser<?> user) {
        assert user != null;
        
        List<User<?>> result = Lists.newArrayList();
        
        if(user instanceof Container<?>){
            if(user instanceof Truck){
                assert trucks.containsKey(user);
                
                result.add(trucks.get(user));
                trucks.remove(user);
            }
            if(user instanceof Depot){
                assert depots.containsKey(user);
                
                result.add(depots.get(user));
                depots.remove(user);
            }
        }
        else if(user instanceof PickupPoint){
            assert pickups.containsKey(user);
            
            result.add(pickups.get(user));
            pickups.remove(user);
        }
        else if(user instanceof DeliveryPoint){
            assert deliveries.containsKey(user);
            
            result.add(deliveries.get(user));
            deliveries.remove(user);
        }
        else{
            throw new IllegalArgumentException("unknown type received..");
        }
        
        return result;
    }

    @Override
    public Class<PdpUser<?>> getSupportedType() {
        return (Class) PdpUser.class;
    }

    @Override
    public void tick(TimeInterval time) {
        this.time = time;
        
        for(PickupPoint<?> p:pickupEvents){
           observer.packagePickedUp(p); 
        }
        pickupEvents.clear();
        
        for(DeliveryPoint<?> d:deliveryEvents){
            observer.packageDelivered(d);
        }
        deliveryEvents.clear();
    }

    @Override
    public void setSeed(long seed) {
        
    }
}
