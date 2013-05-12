package naive;

import java.nio.channels.IllegalSelectorException;

import rinde.sim.core.graph.Point;
import rinde.sim.core.model.Agent;
import rinde.sim.core.model.pdp.Parcel;
import rinde.sim.core.model.pdp.users.Truck;
import rinde.sim.core.model.pdp.users.TruckData;
import rinde.sim.core.simulation.TimeLapse;

public class NaiveTruck extends Truck<TruckData> implements Agent{
	
	private State state;
	
	private enum State{
		SEARCHING,
		DRIVING_TO_PICKUP,
		DRIVING_TO_DELIVERY;
	}
	
	public NaiveTruck() {
		super();
		state = State.SEARCHING;
	}
	
	@Override
	public void tick(TimeLapse time) {
		roadAPI.advance(time);	//Drive as far as possible
		
		if(roadAPI.isDriving() || !time.hasTimeLeft())
			return;
		
		switch(state){
		case SEARCHING:
			Point closest = truckAPI.findClosestAvailableParcel(time);
			if(closest != null){
				roadAPI.setTarget(closest);
				changeState(State.DRIVING_TO_PICKUP);
			}
			else{
				roadAPI.setTarget(roadAPI.getRandomLocation());
			}
			break;
		case DRIVING_TO_PICKUP:
			Parcel pickedParcel = containerAPI.tryPickup(time);
			if(pickedParcel != null){	
				roadAPI.setTarget(pickedParcel.destination);
				changeState(State.DRIVING_TO_DELIVERY);
			}
			else{
				changeState(State.SEARCHING);
			}
			break;
		case DRIVING_TO_DELIVERY:
			Parcel deliveredParcel = containerAPI.tryDelivery(time);
			if(deliveredParcel == null) throw new IllegalStateException();
			changeState(State.SEARCHING);
			break;
		}
	}
	
	private void changeState(State newState){
		this.state = newState;
	}
}
