package rinde.sim.core.model.pdp;

import java.util.Iterator;

import rinde.sim.core.model.pdp.twpolicy.TimeWindowPolicy;
import rinde.sim.core.model.pdp.users.Container;
import rinde.sim.core.model.pdp.users.DeliveryPoint;
import rinde.sim.core.model.pdp.users.Depot;
import rinde.sim.core.model.pdp.users.PickupPoint;
import rinde.sim.core.model.pdp.users.Truck;

public interface PdpAPI{
    
    TimeWindowPolicy getPolicy();
    
    Iterator<Truck<?>> queryTrucks();
    
    Iterator<Depot<?>> queryDepots();
    
    Iterator<PickupPoint<?>> queryPickups();
    
    Iterator<DeliveryPoint<?>> queryDeliveries();
    
    Iterator<Container<?>> queryContainers();

}
