package rinde.sim.core.model.pdp;

import rinde.sim.core.model.pdp.users.DeliveryPoint;
import rinde.sim.core.model.pdp.users.PickupPoint;

public interface PdpObserver {

   void packagePickedUp(PickupPoint<?> p);
   
   void packageDelivered(DeliveryPoint<?> d);
   
}
