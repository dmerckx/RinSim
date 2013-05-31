package gradient2;

import gradient2.FieldTruck2.FTData2;

import java.util.List;
import java.util.Random;

import rinde.sim.core.graph.Point;
import rinde.sim.core.model.Agent;
import rinde.sim.core.model.pdp.Parcel;
import rinde.sim.core.model.pdp.users.Truck;
import rinde.sim.core.model.pdp.users.TruckData;
import rinde.sim.core.simulation.TimeLapse;

public class FieldTruck2 extends Truck<FTData2> implements FieldEmitter2<FTData2>, Agent{
	public static int mode = 0;
	
	private GradientModel2 gradientAPI;
	private Random rand = new Random();
	
	@Override
	public void setGradientModel(GradientModel2 model) {
		this.gradientAPI = model;
	}

	@Override
	public boolean isActive() {
		return true;
	}

	@Override
	public void tick(TimeLapse lapse) {
		if(mode == 0){
			if(lapse.getStartTime() % 2 == 0){
				gradientAPI.getTargetFor(this, 2);
				gradientAPI.getTargetFor(this, 2);
			}
			if(lapse.getStartTime() % 2 == 1){
				truckAPI.findClosestAvailableParcel();
				truckAPI.findClosestAvailableParcel();
			}
		}
		if(mode == 1){
			if(rand.nextBoolean()){
				gradientAPI.getTargetFor(this, 2);
				gradientAPI.getTargetFor(this, 2);
			}
			else{
				truckAPI.findClosestAvailableParcel();
				truckAPI.findClosestAvailableParcel();
			}
		}
		if(mode == 2){
			truckAPI.findClosestAvailableParcel();
			gradientAPI.getTargetFor(this, 2);
		}
		
		/*if(mode == 0 || mode == 2) truckAPI.findClosestAvailableParcel();
		if(mode == 1 || mode == 2) gradientAPI.getTargetFor(this, 2);*/
		/*gradientAPI.getTargetFor(this, 2);
		roadAPI.setTarget(roadAPI.getRandomLocation());
		roadAPI.advance(lapse);*/
		
		/*long result = 0;
		for(long i = 0; i < 100; i++){
			for(long j = 0; j < 1000; j++){
				result = Math.max(i, i+1);
			}
		}*/
		
		/*List<Parcel> load = containerAPI.getState().getLoad();
		
		if(load.size() != 0){
			//deliver the contained package
			Point target = load.get(0).destination;
			roadAPI.setTarget(target);
			roadAPI.advance(lapse);
			
			if(roadAPI.getCurrentLocation().equals(target)){
				//containerAPI.tryDelivery(lapse);
			}
			
			return;
		}
		
		Point p = truckAPI.findClosestAvailableParcel();
		
		if(p != null && Point.distance(p, roadAPI.getCurrentLocation()) < 5){
			//drive to the most nearby package
			roadAPI.setTarget(p);
			roadAPI.advance(lapse);
			if(lapse.hasTimeLeft()){
				//containerAPI.tryPickup(lapse);
			}
		}
		else{
			//let the field guide the way
			Point target = gradientAPI.getTargetFor(this, lapse.getTimeLeft() * roadAPI.getSpeed());
			if(target == null)
				throw new IllegalStateException();
			
			roadAPI.setTarget(target);
			roadAPI.advance(lapse);
		}*/
	}
	
	public static class FTData2 extends TruckData.Std implements FieldData2{
		private final double strength;
		
		public FTData2(double speed, Point pos, double cap, double strenght) {
			super(speed, pos, cap);
			this.strength = strenght;
		}
	
		@Override
		public double getStrenght() {
			return strength;
		}
	}
}