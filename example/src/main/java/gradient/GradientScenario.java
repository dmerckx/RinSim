package gradient;

import comparison.Scenario;

import rinde.sim.core.graph.Point;
import rinde.sim.core.model.pdp.Parcel;
import rinde.sim.core.model.pdp.users.DeliveryPoint;
import rinde.sim.core.model.pdp.users.DeliveryPointData;
import rinde.sim.core.simulation.policies.AgentsPolicy;

public class GradientScenario extends Scenario{
	
	public static final int TRUCK_STRENGTH = -2;
	public static final int PARCEL_STRENGTH = 5;
	
	public GradientScenario(long seed, AgentsPolicy policy, int speed, int ticks, int cars, int proportion) {
		super(seed, policy, speed, ticks, cars, proportion);
	}

	@Override
	protected void registerModels() {
		GradientModel gm = new GradientModel(roadModel);
		
		sim.registerModel(gm);
	}

	@Override
	protected void registerTruck(Point pos, int speed, int cap) {
		sim.registerUser(
				new FieldTruck(),
				new FieldTruck.FTData(speed, pos, 1, TRUCK_STRENGTH));
	}

	@Override
	protected void registerParcel(Parcel p) {
		sim.registerUser(
				new FieldPickPoint(),
				new FieldPickPoint.FPData(p, PARCEL_STRENGTH));
		sim.registerUser(
				new DeliveryPoint.Std(),
				new DeliveryPointData.Std(p));
	}

}
