package blocksize;

import rinde.sim.core.graph.Point;
import rinde.sim.core.model.Agent;
import rinde.sim.core.model.pdp.Parcel;
import rinde.sim.core.model.pdp.users.DeliveryPoint;
import rinde.sim.core.model.pdp.users.DeliveryPointData;
import rinde.sim.core.model.pdp.users.PickupPoint;
import rinde.sim.core.model.pdp.users.PickupPointData;
import rinde.sim.core.model.pdp.users.Truck;
import rinde.sim.core.model.pdp.users.TruckData;
import rinde.sim.core.simulation.TimeLapse;
import rinde.sim.core.simulation.policies.AgentsPolicy;

import comparison.Scenario;

public class BlocksizeScenario extends Scenario{

	public BlocksizeScenario(long seed, AgentsPolicy policy, double speed, int ticks, int cars, double proportion,
			double closestPackageRange) {
		super(seed, policy, speed, ticks, cars, proportion, closestPackageRange);
	}


	@Override
	protected void registerModels() {
		
	}

	@Override
	protected void registerTruck(Point pos, double speed, int cap) {
		sim.registerUser(
				new BSTruck(),
				new TruckData.Std(speed, pos, 1));
	}

	@Override
	protected void registerParcel(Parcel p) {
		sim.registerUser(
				new PickupPoint.Std(),
				new PickupPointData.Std(p));
		sim.registerUser(
				new DeliveryPoint.Std(),
				new DeliveryPointData.Std(p));
	}
}

class BSTruck extends Truck<TruckData> implements Agent{
	
	private State state;
	
	private enum State{
		SEARCHING,
		DRIVING_TO_PICKUP,
		DRIVING_TO_DELIVERY;
	}
	
	public BSTruck() {
		super();
		state = State.SEARCHING;
	}
	
	@Override
	public void tick(TimeLapse time) {
		Point closest = truckAPI.findClosestAvailableParcel();
		roadAPI.advance(time);	//Drive as far as possible
		
		if(roadAPI.isDriving() || !time.hasTimeLeft())
			return;
		
		switch(state){
		case SEARCHING:
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