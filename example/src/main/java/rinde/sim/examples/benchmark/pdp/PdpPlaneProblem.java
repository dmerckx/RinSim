package rinde.sim.examples.benchmark.pdp;

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
import rinde.sim.core.model.pdp.users.PickupPointData;
import rinde.sim.core.model.pdp.users.TruckData;
import rinde.sim.core.model.road.PlaneRoadModel;
import rinde.sim.core.model.road.RoadModel;
import rinde.sim.core.simulation.Simulator;
import rinde.sim.core.simulation.policies.AgentsPolicy;
import rinde.sim.examples.pdp.PdpTruck;
import rinde.sim.ui.View;
import rinde.sim.ui.renderers.PDPModelRenderer;
import rinde.sim.ui.renderers.PlaneRoadModelRenderer;
import rinde.sim.util.TimeWindow;

/**
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 */
public class PdpPlaneProblem implements PdpObserver{
	
	private static final int STEP = 1;

	private Simulator sim;
	
	private final int ticks;
	
	private final int speed;
	private final int nrTrucks;
	private final int proportion;
	private RandomGenerator rng;
	
	private RoadModel roadModel;
	
	private double interactions = 0;
	
	public PdpPlaneProblem(long seed, AgentsPolicy policy, int speed, int ticks, int cars, int proportion){
		this.sim = new Simulator(STEP, seed, policy);

		this.ticks = ticks;
		this.speed = speed;
		this.nrTrucks = cars;
		this.proportion = proportion;
		
		this.rng = new MersenneTwister(seed);
	}
	
	public double run(){
		while(sim.getCurrentTime() < ticks * STEP){
			sim.advanceTick();
		}
		//System.out.println("speed: " + speed + "  interactions: " + ((interactions / ticks) / nrTrucks));
		
		return ((interactions / ticks) / nrTrucks);
	}

	@Override
	public void packagePickedUp(PickupPoint<?> p) {
		sim.unregisterUser(p);
		interactions++;
	}

	@Override
	public void packageDelivered(DeliveryPoint<?> d) {
		sim.unregisterUser(d);
		interactions++;
		addParcel();
	}

	public void init() {
		double z = Math.sqrt(nrTrucks);
		roadModel = new PlaneRoadModel(new Point(0, 0), new Point(380, 380), false, 100);
		InteractionModel interModel = new InteractionModel();
		PdpModel pdpModel = new PdpModel(new LiberalPolicy(), this);
		
		sim.registerModel(roadModel);
		sim.registerModel(interModel);
		sim.registerModel(pdpModel);
		sim.configure();
		

		for (int i = 0; i < nrTrucks; i++) {
			sim.registerUser(
					new PdpTruck(),
					new TruckData.Std(speed, roadModel.getRandomPosition(rng), 1));
		}

		for (int i = 0; i < nrTrucks * proportion; i++) {
			addParcel();
		}
		//View.startGui(sim, 10, new PlaneRoadModelRenderer(), new PDPModelRenderer());
	}
	
	public void close(){
		sim.shutdown();
	}
		
	private void addParcel(){
		Point from = roadModel.getRandomPosition(rng);
		Point to = roadModel.getRandomPosition(rng);
		final Parcel parcel =
				new Parcel(from, to, STEP*2, TimeWindow.ALWAYS, STEP*2, TimeWindow.ALWAYS, 1);
		
		//Register pickup point
		sim.registerUser(new PickupPoint.Std(), new PickupPointData.Std(parcel));
		
		//Register delivery point
		sim.registerUser(new DeliveryPoint.Std(), new DeliveryPointData.Std(parcel));
	}
}
