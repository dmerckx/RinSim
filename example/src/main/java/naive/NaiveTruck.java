package naive;

import rinde.sim.core.graph.Point;
import rinde.sim.core.model.Agent;
import rinde.sim.core.model.pdp.Parcel;
import rinde.sim.core.model.pdp.users.Truck;
import rinde.sim.core.model.pdp.users.TruckData;
import rinde.sim.core.simulation.TimeLapse;

public class NaiveTruck extends Truck<TruckData> implements Agent{

	
	public static double NR_PICKUPS = 0;
	public static double NR_DELIVERIES = 0;
	public static double TOTAL_TIME_PICKING_UP = 0;
	public static double TOTAL_TIME_DELIVERING = 0;
	public static double getAvgTimeToPickup(){
		return TOTAL_TIME_PICKING_UP / NR_PICKUPS;
	}
	public static double getAvgTimeToDeliver(){
		return TOTAL_TIME_DELIVERING / NR_DELIVERIES;
	}
	public long lastPickupTime = 0;
	public long lastDeliveryTime = 0;
	
	
	private State state;
	private long stateChanged = 0;
	
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
		
		if((roadAPI.isDriving() || !time.hasTimeLeft())
				&& (state != State.SEARCHING || time.getCurrentTime() < stateChanged + 40))
			return;
		
		switch(state){
		case SEARCHING:
			Point closest = truckAPI.findClosestAvailableParcel();
			if(closest != null){
				roadAPI.setTarget(closest);
				changeState(State.DRIVING_TO_PICKUP, time);
			}
			else{
				roadAPI.setTarget(roadAPI.getRandomLocation());
			}
			break;
		case DRIVING_TO_PICKUP:
			Parcel pickedParcel = containerAPI.tryPickup(time);
			if(pickedParcel != null){	
				roadAPI.setTarget(pickedParcel.destination);
				changeState(State.DRIVING_TO_DELIVERY, time);
				
				TOTAL_TIME_PICKING_UP += time.getCurrentTime() - lastDeliveryTime;
				NR_PICKUPS++;
				lastPickupTime = time.getCurrentTime();
			}
			else{
				changeState(State.SEARCHING, time);
			}
			break;
		case DRIVING_TO_DELIVERY:
			Parcel deliveredParcel = containerAPI.tryDelivery(time);
			if(deliveredParcel == null) throw new IllegalStateException();
			changeState(State.SEARCHING, time);
			
			TOTAL_TIME_DELIVERING += time.getCurrentTime() - lastPickupTime;
			NR_DELIVERIES++;
			lastDeliveryTime = time.getCurrentTime();
			break;
		}
	}
	
	private void changeState(State newState, TimeLapse time){
		this.state = newState;
		this.stateChanged = time.getCurrentTime();
	}
}
