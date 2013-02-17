package rinde.sim.examples.pdp;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;

import rinde.sim.core.graph.Graph;
import rinde.sim.core.graph.MultiAttributeData;
import rinde.sim.core.graph.Point;
import rinde.sim.core.model.interaction.InteractionModel;
import rinde.sim.core.model.pdp.Parcel;
import rinde.sim.core.model.pdp.PdpModel;
import rinde.sim.core.model.pdp.PdpObserver;
import rinde.sim.core.model.pdp.twpolicy.LiberalPolicy;
import rinde.sim.core.model.pdp.users.DeliveryPoint;
import rinde.sim.core.model.pdp.users.DeliveryPointData;
import rinde.sim.core.model.pdp.users.PickupPoint;
import rinde.sim.core.model.pdp.users.PickupPointData;
import rinde.sim.core.model.pdp.users.TruckData;
import rinde.sim.core.model.road.GraphRoadModel;
import rinde.sim.core.model.road.RoadModel;
import rinde.sim.core.simulation.Simulator;
import rinde.sim.serializers.DotGraphSerializer;
import rinde.sim.serializers.SelfCycleFilter;
import rinde.sim.ui.View;
import rinde.sim.ui.renderers.GraphRoadModelRenderer;
import rinde.sim.ui.renderers.PDPModelRenderer;
import rinde.sim.util.TimeWindow;

/**
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 * 
 */
public class ExamplePDP implements PdpObserver{
	
	private static final int STEP = 10000;
	private Simulator sim;
	
	public ExamplePDP(Simulator sim) {
		this.sim = sim;
	}

	@Override
	public void packagePickedUp(PickupPoint<?> p) {
		System.out.println("Package picked up");
		sim.unregisterUser(p);
	}

	@Override
	public void packageDelivered(DeliveryPoint<?> d) {
		System.out.println("Package delivered");
		sim.unregisterUser(d);
	}

	public static void main(String[] args) throws FileNotFoundException, IOException {

		final String MAP_DIR = "../core/files/maps/";
		// create a new simulator, load map of Leuven
		final RandomGenerator rng = new MersenneTwister(123);
		final Simulator simulator = new Simulator(STEP);
		final Graph<MultiAttributeData> graph = DotGraphSerializer
				.getMultiAttributeGraphSerializer(new SelfCycleFilter()).read(MAP_DIR + "leuven-simple.dot");
		final RoadModel roadModel = new GraphRoadModel(graph);
		final InteractionModel interModel = new InteractionModel();
		final PdpModel pdpModel = new PdpModel(new LiberalPolicy(), new ExamplePDP(simulator));
		
		simulator.registerModel(roadModel);
		simulator.registerModel(interModel);
		simulator.registerModel(pdpModel);
		simulator.configure();
		

		for (int i = 0; i < 10; i++) {
			simulator.registerUser(
					new PdpTruck(),
					new TruckData.Std(1000, roadModel.getRandomPosition(rng), 100));
		}

		for (int i = 0; i < 50; i++) {
			Point from = roadModel.getRandomPosition(rng);
			Point to = roadModel.getRandomPosition(rng);
			final Parcel parcel =
					new Parcel(from, to, STEP * 3, TimeWindow.ALWAYS, STEP * 2, TimeWindow.ALWAYS, 1);
			
			//Register pickup point
			simulator.registerUser(new PickupPoint.Std(), new PickupPointData.Std(parcel));
			
			//Register delivery point
			simulator.registerUser(new DeliveryPoint.Std(), new DeliveryPointData.Std(parcel));
		}
		
		//simulator.start();
		View.startGui(simulator, 1, new GraphRoadModelRenderer(), new PDPModelRenderer());
	}
}
