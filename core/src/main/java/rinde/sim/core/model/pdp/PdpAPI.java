package rinde.sim.core.model.pdp;

import rinde.sim.core.model.SafeIterator;
import rinde.sim.core.model.pdp.twpolicy.TimeWindowPolicy;
import rinde.sim.core.model.pdp.users.Container;
import rinde.sim.core.model.pdp.users.DeliveryPoint;
import rinde.sim.core.model.pdp.users.Depot;
import rinde.sim.core.model.pdp.users.PickupPoint;
import rinde.sim.core.model.pdp.users.Truck;

public interface PdpAPI{
    
    TimeWindowPolicy getPolicy();
    
    SafeIterator<Truck<?>> queryTrucks();
    
    SafeIterator<Depot<?>> queryDepots();
    
    SafeIterator<PickupPoint<?>> queryPickups();
    
    SafeIterator<DeliveryPoint<?>> queryDeliveries();
    
    SafeIterator<Container<?>> queryContainers();
}
