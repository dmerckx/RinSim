package gradient.test;

import gradient.FieldPickPoint;
import gradient.FieldTruck;
import gradient.GradientModel;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;

import rinde.sim.core.graph.Point;
import rinde.sim.core.model.interaction.InteractionModel;
import rinde.sim.core.model.pdp.Parcel;
import rinde.sim.core.model.pdp.PdpModel;
import rinde.sim.core.model.pdp.PdpObserver;
import rinde.sim.core.model.pdp.twpolicy.LiberalPolicy;
import rinde.sim.core.model.pdp.users.DeliveryPoint;
import rinde.sim.core.model.pdp.users.DeliveryPointData;
import rinde.sim.core.model.pdp.users.PickupPoint;
import rinde.sim.core.model.road.PlaneRoadModel;
import rinde.sim.core.model.road.RoadModel;
import rinde.sim.core.simulation.Simulator;
import rinde.sim.core.simulation.policies.AgentsPolicy;
import rinde.sim.core.simulation.policies.agents.ModPoolSingle;
import rinde.sim.core.simulation.policies.agents.SingleThreaded;
import rinde.sim.ui.View;
import rinde.sim.ui.renderers.PDPModelRenderer;
import rinde.sim.ui.renderers.PlaneRoadModelRenderer;
import rinde.sim.util.TimeWindow;


/**
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 * 
 */
public class ExampleGradient implements PdpObserver{
	private static final int STEP = 1;
	
	//private static final int STEP = 10000;
	private static final int TRUCKS = 5;
	private static final int PACKAGES = 35;
	
	private static final double TRUCK_STRENGHT = -1.0d;
	private static final double PICKUP_STRENGHT = 5.0d; 
	
	private Simulator sim;
	private int packages;
	
	public ExampleGradient(Simulator sim) {
		this.sim = sim;
		this.packages = PACKAGES;
	}

	@Override
	public void packagePickedUp(PickupPoint<?> p) {
		//System.out.println("Package picked up");
		//sim.unregisterUser(p);
	}

	@Override
	public void packageDelivered(DeliveryPoint<?> d) {
		//System.out.println("Package delivered");
		//sim.unregisterUser(d);
		packages--;
	}
	
	public boolean isDone(){
		return false;
		//return packages == 0;
		//return sim.getCurrentTime() > 3200 * STEP * 5;
	}

	public static void main(String[] args) throws FileNotFoundException, IOException {
		final String MAP_DIR = "../core/files/maps/";
		// create a new simulator, load map of Leuven
		final RandomGenerator rng = new MersenneTwister(123);
		
		//-----SIMULATOR-----//
		AgentsPolicy policy = new SingleThreaded();
		final Simulator simulator = new Simulator(STEP, policy);

		//-----ROAD MODEL-----//
		final RoadModel roadModel = new PlaneRoadModel(new Point(0, 0), new Point(100,100), false, 100);

		//-----INTERACTION MODEL-----//
		final InteractionModel interModel = new InteractionModel();
		
		//-----PDP MODEL-----//
		final PdpObserver obs = new ExampleGradient(simulator);
		final PdpModel pdpModel = new PdpModel(new LiberalPolicy(), obs);
		//final PdpModel pdpModel = new CachedPDPModel(new LiberalPolicy(), obs, roadModel);
		
		//-----GRADIENT FIELD MODEL-----//
		final GradientModel gm = new GradientModel(roadModel);
		
		simulator.registerModel(roadModel);
		simulator.registerModel(interModel);
		simulator.registerModel(pdpModel);
		simulator.registerModel(gm);
		simulator.configure();	

		for (int i = 0; i < TRUCKS; i++) {
			Point pos = roadModel.getRandomPosition(rng);
			
			simulator.registerUser(
					new FieldTruck(),
					new FieldTruck.FTData(1, pos, 1, TRUCK_STRENGHT));
		}

		for (int i = 0; i < PACKAGES; i++) {
			Point from = roadModel.getRandomPosition(rng);
			Point to = roadModel.getRandomPosition(rng);
			final Parcel parcel =
					new Parcel(from, to, STEP * 3, TimeWindow.ALWAYS, STEP * 2, TimeWindow.ALWAYS, 1);
			
			//Register pickup point
			simulator.registerUser(
					new FieldPickPoint(),
					new FieldPickPoint.FPData(parcel, PICKUP_STRENGHT));
			
			//Register delivery point
			simulator.registerUser(new DeliveryPoint.Std(), new DeliveryPointData.Std(parcel));
		}
		
		View.startGui(simulator, 10, new PlaneRoadModelRenderer(), new PDPModelRenderer());
		
		//simulator.advanceTick();
		/*long start = System.currentTimeMillis();
		while(!obs.isDone()){
			simulator.advanceTick();
		}
		Monitor.get().printReport();
		simulator.shutdown();
		System.out.println("Total time: " + (System.currentTimeMillis() - start));
		System.out.println("Interactions: " + (interModel.getAverageInteractions() / 500));
		System.out.println("DONE!" + (simulator.getCurrentTime() / STEP));*/
	}
}
