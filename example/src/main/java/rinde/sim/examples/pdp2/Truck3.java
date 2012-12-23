package rinde.sim.examples.pdp2;

import rinde.sim.core.model.Agent;
import rinde.sim.core.model.pdp.Parcel;
import rinde.sim.core.model.pdp.users.Truck;
import rinde.sim.core.model.pdp.users.TruckData;
import rinde.sim.core.simulation.TimeLapse;

public class Truck3 extends Truck<TruckData> implements Agent{
	
	Parcel target = null;
	State state = State.LOOKING;
	
	private enum State{
		LOOKING,
		GOING_TO_PICKUP,
		GOING_TO_DELIVER;
	}
	
	public Truck3() {
		super();
	}
	
	@Override
	public void tick(TimeLapse time) {
		roadAPI.advance(time);	//Drive as far as possible
		
		if(roadAPI.isDriving())
			return;
		
		if(target == null){		//Search a new target
			target = truckAPI.findClosestAvailableParcel(time);
			if(target != null) {
				roadAPI.setTarget(target.location);
				state = State.GOING_TO_PICKUP;
				tick(time);
				return;
			}
			else {
				roadAPI.setTarget(roadAPI.getRandomLocation());
				return;
			}
		}
		else {					//Target is reached
			switch(state){
				case GOING_TO_PICKUP:
					Parcel pickedUp = containerAPI.tryPickup(time);
					if(pickedUp == null){
						state = State.LOOKING;
						target = null;
						tick(time);
						return;
					}
					else {
						state = State.GOING_TO_DELIVER;
						roadAPI.setTarget(target.destination);
						tick(time);
					}
					break;
				case GOING_TO_DELIVER:
					Parcel delivered = containerAPI.tryDelivery(time);
					System.out.println("Delivered: " + delivered);
					target = null;
					state = State.LOOKING;
					break;
			}
		}
	}
}
