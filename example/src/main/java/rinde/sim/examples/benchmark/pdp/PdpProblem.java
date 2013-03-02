package rinde.sim.examples.benchmark.pdp;

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
import rinde.sim.examples.pdp.PdpTruck;
import rinde.sim.serializers.DotGraphSerializer;
import rinde.sim.serializers.SelfCycleFilter;
import rinde.sim.util.TimeWindow;

/**
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 */
public class PdpProblem implements PdpObserver{
	
	private static final int STEP = 10000;
	
	private int nrTrucks;
	private int nrParcels;
	
	private Simulator sim;
	
	
	public PdpProblem(int cars, int packages, long seed) {
		this.nrTrucks = cars;
		this.nrParcels = packages;
		
		sim = new Simulator(STEP);
		try{
			init(seed);
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public void run(){
		while(nrParcels > 0){
			sim.advanceTick();
		}
	}

	@Override
	public void packagePickedUp(PickupPoint<?> p) {
		sim.unregisterUser(p);
	}

	@Override
	public void packageDelivered(DeliveryPoint<?> d) {
		sim.unregisterUser(d);
		nrParcels--;
	}

	public void init(long seed) throws FileNotFoundException, IOException {

		final String MAP_DIR = "../core/files/maps/";
		// create a new simulator, load map of Leuven
		final RandomGenerator rng = new MersenneTwister(seed);
		final Graph<MultiAttributeData> graph = DotGraphSerializer
				.getMultiAttributeGraphSerializer(new SelfCycleFilter()).read(MAP_DIR + "leuven-simple.dot");
		final RoadModel roadModel = new GraphRoadModel(graph);
		final InteractionModel interModel = new InteractionModel();
		final PdpModel pdpModel = new PdpModel(new LiberalPolicy(), this);
		
		sim.registerModel(roadModel);
		sim.registerModel(interModel);
		sim.registerModel(pdpModel);
		sim.configure();
		

		for (int i = 0; i < nrTrucks; i++) {
			sim.registerUser(
					new PdpTruck(),
					new TruckData.Std(1000, roadModel.getRandomPosition(rng), 100));
		}

		for (int i = 0; i < nrParcels; i++) {
			Point from = roadModel.getRandomPosition(rng);
			Point to = roadModel.getRandomPosition(rng);
			final Parcel parcel =
					new Parcel(from, to, STEP * 3, TimeWindow.ALWAYS, STEP * 2, TimeWindow.ALWAYS, 1);
			
			//Register pickup point
			sim.registerUser(new PickupPoint.Std(), new PickupPointData.Std(parcel));
			
			//Register delivery point
			sim.registerUser(new DeliveryPoint.Std(), new DeliveryPointData.Std(parcel));
		}
	}
}
