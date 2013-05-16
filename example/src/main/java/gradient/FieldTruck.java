package gradient;

import gradient.FieldTruck.FTData;
import gradient.model.apis.GradientAPI;
import gradient.model.apis.GradientState;
import gradient.model.users.FieldData;
import gradient.model.users.FieldEmitter;
import rinde.sim.core.graph.Point;
import rinde.sim.core.model.Agent;
import rinde.sim.core.model.pdp.Parcel;
import rinde.sim.core.model.pdp.users.Truck;
import rinde.sim.core.model.pdp.users.TruckData;
import rinde.sim.core.simulation.TimeLapse;

public class FieldTruck extends Truck<FTData> implements FieldEmitter<FTData>, Agent{
	
	private GradientAPI gradientAPI;
	private State state;
	
	private enum State{
		SEARCHING,
		DRIVING_TO_DELIVERY;
	}
	
	public FieldTruck() {
		state = State.SEARCHING;
	}
	
	@Override
	public void setGradientAPI(GradientAPI api) {
		api.init(roadAPI);
		this.gradientAPI = api;
	}
	
	@Override
	public void tick(TimeLapse time) {
		switch(state){
		case SEARCHING:
			Point closest = truckAPI.findClosestAvailableParcel();

			if(closest != null && Point.distance(closest, roadAPI.getCurrentLocation()) < 3 * roadAPI.getSpeed()){
				//drive to the most nearby package
				roadAPI.setTarget(closest);
				roadAPI.advance(time);

				if(!roadAPI.isDriving() && time.hasTimeLeft()){
					Parcel p = containerAPI.tryPickup(time);
					if(p != null){
						roadAPI.setTarget(p.destination);
						changeState(State.DRIVING_TO_DELIVERY);
					}
				}
			}
			else{
				//let the field guide the way
				Point target = gradientAPI.getTarget(roadAPI.getSpeed());
				if(target == null) throw new IllegalStateException();
				
				roadAPI.setTarget(target);
				roadAPI.advance(time);
			}
			break;
		case DRIVING_TO_DELIVERY:
			roadAPI.advance(time);

			if(!roadAPI.isDriving() && time.hasTimeLeft()){
				Parcel deliveredParcel = containerAPI.tryDelivery(time);
				if(deliveredParcel == null) throw new IllegalStateException();
				changeState(State.SEARCHING);
			}
			break;
		}
	}
	
	private void changeState(State newState){
		//Todo in originele RinSim versie
		gradientAPI.setIsActive(state == State.SEARCHING);
		
		this.state = newState;
	}
	
	public static class FTData extends TruckData.Std implements FieldData{
		private final double strength;
		
		public FTData(double speed, Point pos, double cap, double strenght) {
			super(speed, pos, cap);
			this.strength = strenght;
		}
	
		@Override
		public double getStrenght() {
			return strength;
		}
	}

	@Override
	public GradientState getGradientState() {
		return gradientAPI.getState();
	}
}