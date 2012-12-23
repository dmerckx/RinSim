package rinde.sim.examples.pdp2;

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
import rinde.sim.core.model.pdp.users.ContainerData;
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
import rinde.sim.ui.renderers.RoadUserRenderer;
import rinde.sim.ui.renderers.UiSchema;
import rinde.sim.util.TimeWindow;

/**
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 * 
 */
public class Pdp2Example implements PdpObserver{
	
	private Simulator sim;
	
	public Pdp2Example(Simulator sim) {
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
		final Simulator simulator = new Simulator(10000);
		final Graph<MultiAttributeData> graph = DotGraphSerializer
				.getMultiAttributeGraphSerializer(new SelfCycleFilter()).read(MAP_DIR + "leuven-simple.dot");
		final RoadModel roadModel = new GraphRoadModel(graph);
		final InteractionModel interModel = new InteractionModel();
		final PdpModel pdpModel = new PdpModel(new LiberalPolicy(), new Pdp2Example(simulator));
		
		simulator.registerModel(roadModel);
		simulator.registerModel(interModel);
		simulator.registerModel(pdpModel);
		simulator.configure();

		for (int i = 0; i < 0; i++) {
			simulator.registerUser(new Depot2(), new ContainerData() {
				@Override
				public Point getStartPosition() {
					return roadModel.getRandomPosition(rng);
				}
				@Override
				public Class<? extends Parcel> getParcelType() {
					return Parcel.class;
				}
				@Override
				public double getCapacity() {
					return 100;
				}
			});
		}

		for (int i = 0; i < 5; i++) {
			simulator.registerUser(new Truck2(), new TruckData() {
				@Override
				public double getInitialSpeed() {
					return 1000;
				}
				@Override
				public Point getStartPosition() {
					return roadModel.getRandomPosition(rng);
				}
				
				@Override
				public Class<? extends Parcel> getParcelType() {
					return Parcel.class;
				}
				
				@Override
				public double getCapacity() {
					return 10;
				}
			});
		}

		for (int i = 0; i < 40; i++) {
			Point from = roadModel.getRandomPosition(rng);
			Point to = roadModel.getRandomPosition(rng);
			final Parcel parcel =
					new Parcel(from, to, 10, TimeWindow.ALWAYS, 10, TimeWindow.ALWAYS, 10.0);
			
			//Register pickup point
			simulator.registerUser(new PickupPoint2(), new PickupPointData() {
				@Override
				public Parcel getParcel() {
					return parcel;
				}
			});
			
			//Register delivery point
			simulator.registerUser(new DeliveryPoint2(), new DeliveryPointData() {
				@Override
				public Parcel getParcel() {
					return parcel;
				}
			});
		}

//		simulator.start();
		final UiSchema uis = new UiSchema();
		uis.add(Depot2.class, "/graphics/perspective/tall-building-64.png");
		uis.add(Truck2.class, "/graphics/flat/taxi-32.png");
		uis.add(PickupPoint2.class, "/graphics/flat/hailing-cab-32.png");
		View.startGui(simulator, 1, new GraphRoadModelRenderer(), new RoadUserRenderer(uis, false));
	}
}
