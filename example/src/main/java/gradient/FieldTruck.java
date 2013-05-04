package gradient;

import gradient.FieldTruck.FTData;

import java.util.List;

import rinde.sim.core.graph.Point;
import rinde.sim.core.model.Agent;
import rinde.sim.core.model.pdp.Parcel;
import rinde.sim.core.model.pdp.users.Truck;
import rinde.sim.core.model.pdp.users.TruckData;
import rinde.sim.core.simulation.TimeLapse;

public class FieldTruck extends Truck<FTData> implements FieldEmitter<FTData>, Agent{
	private GradientModel gradientAPI;
	
	@Override
	public void setGradientModel(GradientModel model) {
		this.gradientAPI = model;
	}

	@Override
	public boolean isActive() {
		return true;
	}
	
	public int searching = 0;
	public int delivering = 0;
	public int pickups = 0; 
	public int deliveries = 0;
	
	public static double SEARCHING = 0;
	public static double DELIVERIES = 0;

	@Override
	public void tick(TimeLapse lapse) {
		SEARCHING = (searching * 1.0d) / pickups;
		DELIVERIES = (delivering * 1.0d) / deliveries;
		
		List<Parcel> load = containerAPI.getState().getLoad();
		
		if(load.size() != 0){
			delivering++;
			//deliver the contained package
			Point target = load.get(0).destination;
			roadAPI.setTarget(target);
			roadAPI.advance(lapse);
			
			if(roadAPI.getCurrentLocation().equals(target)){
				Parcel p = containerAPI.tryDelivery(lapse);
				if(p != null) deliveries++;
			}
			
			return;
		}
		
		searching++;
		
		Parcel p = truckAPI.findClosestAvailableParcel(lapse);
		
		if(p != null && Point.distance(p.location, roadAPI.getCurrentLocation()) < 5){
			//drive to the most nearby package
			roadAPI.setTarget(p.location);
			roadAPI.advance(lapse);
			if(lapse.hasTimeLeft()){
				Parcel p2 = containerAPI.tryPickup(lapse);
				if(p2 != null) 
					pickups++;
			}
		}
		else{
			//let the field guide the way
			Point target = gradientAPI.getTargetFor(this, lapse.getTimeLeft() * roadAPI.getSpeed());
			if(target == null)
				throw new IllegalStateException();
			
			roadAPI.setTarget(target);
			roadAPI.advance(lapse);
		}
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
}