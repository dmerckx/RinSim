package naive;

import rinde.sim.core.graph.Point;
import rinde.sim.core.model.pdp.Parcel;
import rinde.sim.core.model.pdp.users.DeliveryPoint;
import rinde.sim.core.model.pdp.users.DeliveryPointData;
import rinde.sim.core.model.pdp.users.PickupPoint;
import rinde.sim.core.model.pdp.users.PickupPointData;
import rinde.sim.core.model.pdp.users.TruckData;
import rinde.sim.core.simulation.policies.AgentsPolicy;

import comparison.Scenario;

public class NaiveScenario extends Scenario{

	public NaiveScenario(long seed, AgentsPolicy policy, int speed, int ticks, int cars,
			int proportion, double closestPackageRange) {
		super(seed, policy, speed, ticks, cars, proportion, closestPackageRange);
	}

	@Override
	protected void registerModels() {
		
	}

	@Override
	protected void registerTruck(Point pos, int speed, int cap) {
		sim.registerUser(
				new NaiveTruck(),
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
