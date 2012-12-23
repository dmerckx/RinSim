package rinde.sim.examples.pdp2;

import rinde.sim.core.model.Agent;
import rinde.sim.core.model.pdp.Parcel;
import rinde.sim.core.model.pdp.users.Truck;
import rinde.sim.core.model.pdp.users.TruckData;
import rinde.sim.core.simulation.TimeLapse;

public class Truck2 extends Truck<TruckData> implements Agent{
	
	Parcel target = null;
	
	public Truck2() {
		super();
	}
	
	@Override
	public void tick(TimeLapse time) {
		roadAPI.advance(time);	//Drive as far as possible
		
		if(roadAPI.isDriving())
			return;
		
		if(containerAPI.getCurrentLoad().size() == 0){	//Search new parcel
			if(target != null){
				target = containerAPI.tryPickup(time);
				if(target != null){
					roadAPI.setTarget(target.destination);
				}
			}
			else{
				target = truckAPI.findClosestAvailableParcel(time);
				roadAPI.setTarget(target!=null?target.location:roadAPI.getRandomLocation());
			}
		}
		else{
			target = containerAPI.tryDelivery(time);
		}
	}
}
