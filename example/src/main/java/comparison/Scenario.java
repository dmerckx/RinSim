package comparison;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;

import rinde.sim.core.graph.Point;
import rinde.sim.core.model.interaction.InteractionModel;
import rinde.sim.core.model.pdp.Parcel;
import rinde.sim.core.model.pdp.PdpModel;
import rinde.sim.core.model.pdp.PdpObserver;
import rinde.sim.core.model.pdp.twpolicy.LiberalPolicy;
import rinde.sim.core.model.pdp.users.DeliveryPoint;
import rinde.sim.core.model.pdp.users.PickupPoint;
import rinde.sim.core.model.road.PlaneRoadModel;
import rinde.sim.core.model.road.RoadModel;
import rinde.sim.core.simulation.Simulator;
import rinde.sim.core.simulation.policies.AgentsPolicy;
import rinde.sim.core.simulation.policies.agents.areas.Areas;
import rinde.sim.ui.View;
import rinde.sim.ui.renderers.PDPModelRenderer;
import rinde.sim.ui.renderers.PlaneRoadModelRenderer;
import rinde.sim.util.Rectangle;
import rinde.sim.util.TimeWindow;

public abstract class Scenario  implements PdpObserver{
	
	private static final int STEP = 1;

	protected Simulator sim;
	
	private final int ticks;
	
	private final int speed;
	private final int nrTrucks;
	private final int proportion;
	private RandomGenerator rng;
	
	protected RoadModel roadModel;
	
	private double interactions = 0;
	
	private int pickups = 0;
	private int deliveries = 0;
	
	private Rectangle rect;
	
	public Scenario(long seed, AgentsPolicy policy, int speed, int ticks, int cars, int proportion){
		this.sim = new Simulator(STEP, seed, policy);
		this.ticks = ticks;
		this.speed = speed;
		this.nrTrucks = cars;
		this.proportion = proportion;
		
		this.rng = new MersenneTwister(seed);
	}
	
	public Result run(){
		while(sim.getCurrentTime() < ticks * STEP){
			sim.advanceTick();
		}
		//System.out.println("speed: " + speed + "  interactions: " + ((interactions / ticks) / nrTrucks));
		return new Result(pickups, deliveries, (interactions / ticks) / nrTrucks);
	}
	
	public void runGUI(){
		View.startGui(sim, 10, new PlaneRoadModelRenderer(), new PDPModelRenderer());	
	}
	
	@Override
	public void packagePickedUp(PickupPoint<?> p) {
		sim.unregisterUser(p);
		interactions++;
		pickups++;
	}

	@Override
	public void packageDelivered(DeliveryPoint<?> d) {
		sim.unregisterUser(d);
		interactions++;
		deliveries++;
		addParcel();
	}

	public void init() {
		double z = Math.max(100, Math.sqrt(nrTrucks) * 50);
		roadModel = new PlaneRoadModel(new Point(0, 0), new Point(z, z), false, 100);
		rect = roadModel.getViewRect();
		InteractionModel interModel = new InteractionModel();
		PdpModel pdpModel = new PdpModel(new LiberalPolicy(), this);
		
		sim.registerModel(roadModel);
		sim.registerModel(interModel);
		sim.registerModel(pdpModel);
		
		registerModels();
		
		sim.configureWithWarmup();
		

		for (int i = 0; i < nrTrucks; i++) {
			addTruck();
		}

		for (int i = 0; i < nrTrucks * proportion; i++) {
			addParcel();
		}
	}
	
	private void addTruck(){
		registerTruck(roadModel.getRandomPosition(rng), speed, 1);
	}
	
	private void addParcel(){
		Point from = roadModel.getRandomPosition(rng);
		
		int xMin = (int) Math.max(rect.xMin, from.x - 40) + 1;
		int xMax = (int) Math.min(rect.xMax, from.x + 40) - 1;
		
		int yMin = (int) Math.max(rect.yMin, from.y - 40) + 1;
		int yMax = (int) Math.min(rect.yMax, from.y + 40) - 1;
		
		//System.out.println(xMin + " " + xMax + "," + yMin + " " + yMax);
		
		int x = rng.nextInt(xMax - xMin) + xMin;
		int y = rng.nextInt(yMax - yMin) + yMin;
		
		Point to = new Point(x, y);
		//System.out.println("rect: " + rect);
		//System.out.println("x " + x + " y " + y);
		
		final Parcel parcel =
				new Parcel(from, to, STEP*2, TimeWindow.ALWAYS, STEP*2, TimeWindow.ALWAYS, 1);
	
		registerParcel(parcel);
	}
	
	abstract protected void registerModels();
	abstract protected void registerTruck(Point pos, int speed, int cap);
	abstract protected void registerParcel(Parcel p);
	
	public void close(){
		sim.shutdown();
	}
}

class Result{
	public final int pickups;
	public final int deliveries;
	public final double interactionRate;
	
	public Result(int pickups, int deliveries, double interactionRate){
		this.pickups = pickups;
		this.deliveries = deliveries;
		this.interactionRate = interactionRate;
	}
}
