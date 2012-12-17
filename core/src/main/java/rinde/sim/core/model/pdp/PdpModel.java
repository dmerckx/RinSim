package rinde.sim.core.model.pdp;

import java.util.HashMap;
import java.util.List;

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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

@SuppressWarnings("rawtypes")
public class PdpModel implements Model<Data, PdpUser<?>>, PdpAPI{

    private TimeInterval time;
    private final TimeWindowPolicy twp;
    
    private HashMap<PdpUser<?>, User<?>> mapping = Maps.newHashMap();
    
    public PdpModel(TimeWindowPolicy twp) {
        this.twp = twp;
    }
    
    public TimeWindowPolicy getPolicy(){
        return twp;
    }
    
    public TimeInterval getTime(){
        return time;
    }
    
    // ------ MODEL ------ //

    @Override
    public List<UserInit<?>> register(PdpUser<?> user, Data data) {
        assert user != null;
        assert data != null;
        
        List<UserInit<?>> result = Lists.newArrayList();
        
        if(user instanceof Container<?>){
            ContainerGuard guard = new ContainerGuard((Container) user, (ContainerData) data, this);
            ((Container) user).setContainerAPI(guard);
            
            result.add(UserInit.create(guard));
            mapping.put(user, guard);
            
            if(user instanceof Truck<?>){
                TruckGuard guard2 = new TruckGuard((Truck<?>) user, (TruckData) data, this);
                ((Truck) user).setTruckAPI(guard2);
            }
            else if(user instanceof Depot<?>){
                
            }
        }
        else if(user instanceof PickupPoint){
            PickupGuard guard = new PickupGuard((PickupPoint<?>) user, (PickupPointData) data, this);
            ((PickupPoint) user).setPickupAPI(guard);
            
            result.add(UserInit.create(guard));
            mapping.put(user, guard);
        }
        else if(user instanceof DeliveryPoint){
            DeliveryGuard guard = new DeliveryGuard((DeliveryPoint<?>) user, (DeliveryPointData) data, this);
            ((DeliveryPoint) user).setDeliveryAPI(guard);
            
            result.add(UserInit.create(guard));
            mapping.put(user, guard);
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
        
        if(user instanceof Container<?> 
                || user instanceof PickupPoint 
                || user instanceof DeliveryPoint){
            assert mapping.containsKey(user);
            
            result.add(mapping.get(user));
            mapping.remove(user);
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
    }
}
