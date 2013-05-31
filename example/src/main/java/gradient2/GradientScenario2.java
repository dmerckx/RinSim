package gradient2;

import rinde.sim.core.graph.Point;
import rinde.sim.core.model.pdp.Parcel;
import rinde.sim.core.model.pdp.users.DeliveryPoint;
import rinde.sim.core.model.pdp.users.DeliveryPointData;
import rinde.sim.core.simulation.policies.AgentsPolicy;

import comparison.Scenario;

public class GradientScenario2 extends Scenario{
	
	public static final int TRUCK_STRENGTH = -2;
	public static final int PARCEL_STRENGTH = 5;
	
	public GradientScenario2(long seed, AgentsPolicy policy, double speed, int ticks, int cars, double proportion, double closestPackageRange) {
		super(seed, policy, speed, ticks, cars, proportion, closestPackageRange);
	}

	@Override
	protected void registerModels() {
		GradientModel2 gm = new GradientModel2(roadModel);
		
		sim.registerModel(gm);
	}

	@Override
	protected void registerTruck(Point pos, double speed, int cap){
		sim.registerUser(
				new FieldTruck2(),
				new FieldTruck2.FTData2(speed, pos, 1, TRUCK_STRENGTH));
	}

	@Override
	protected void registerParcel(Parcel p) {
		sim.registerUser(
				new FieldPickPoint2(),
				new FieldPickPoint2.FPData2(p, PARCEL_STRENGTH));
		sim.registerUser(
				new DeliveryPoint.Std(),
				new DeliveryPointData.Std(p));
	}

}
