package contractnet;

import gradient.GradientModel;
import rinde.sim.core.graph.Point;
import rinde.sim.core.model.communication.CommunicationModel;
import rinde.sim.core.model.pdp.Parcel;
import rinde.sim.core.model.pdp.users.DeliveryPoint;
import rinde.sim.core.model.pdp.users.DeliveryPointData;
import rinde.sim.core.simulation.policies.AgentsPolicy;

import comparison.Scenario;

public class ContractScenario extends Scenario{
	
	public final double radius;
	
	public ContractScenario(long seed, AgentsPolicy policy, int speed, int ticks, int cars, int proportion, double radius) {
		super(seed, policy, speed, ticks, cars, proportion);
		this.radius = radius;
	}

	@Override
	protected void registerModels() {
		CommunicationModel cm = new CommunicationModel();
		
		sim.registerModel(cm);
	}

	@Override
	protected void registerTruck(Point pos, int speed, int cap) {
		sim.registerUser(
				new ContractTruck(),
				new ContractTruck.CTTruckData(speed, pos, 1, radius));
	}

	@Override
	protected void registerParcel(Parcel p) {
		sim.registerUser(
				new ContractPickup(),
				new ContractPickup.CTPickupData(p, radius));
		sim.registerUser(
				new DeliveryPoint.Std(),
				new DeliveryPointData.Std(p));
	}


}
