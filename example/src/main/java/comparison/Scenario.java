package comparison;

import gradient.GradientScenario;
import naive.NaiveScenario;

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
import rinde.sim.core.model.road.AbstractRoadModel;
import rinde.sim.core.model.road.PlaneRoadModel;
import rinde.sim.core.simulation.Simulator;
import rinde.sim.core.simulation.policies.AgentsPolicy;
import rinde.sim.ui.View;
import rinde.sim.ui.renderers.PDPModelRenderer;
import rinde.sim.ui.renderers.PlaneRoadModelRenderer;
import rinde.sim.util.Rectangle;
import rinde.sim.util.TimeWindow;
import contractnet.ContractScenario;

public abstract class Scenario  implements PdpObserver{
	
	private static final int STEP = 1;

	protected Simulator sim;
	
	private final int ticks;
	
	private final double speed;
	private final int nrTrucks;
	private final double proportion;
	private final double range;
	private RandomGenerator rng;
	
	protected AbstractRoadModel<?> roadModel;
	
	private double interactions = 0;
	
	private int pickups = 0;
	private int deliveries = 0;
	private int advanced = 0;
	
	private Rectangle rect;
	
	public Scenario(long seed, AgentsPolicy policy, double speed, int ticks, int cars, double proportion, double closestPackageRange){
		this.sim = new Simulator(STEP, seed, policy);
		this.ticks = ticks;
		this.speed = speed;
		this.nrTrucks = cars;
		this.proportion = proportion;
		this.range = closestPackageRange;
		
		this.rng = new MersenneTwister(seed);
	}
	
	public Result run(){
		return run(Long.MAX_VALUE);
	}
	
	public void warmupTicks(int ticks){
		while(sim.getCurrentTime() < ticks * STEP){
			sim.advanceTick();
		}
		
		pickups = 0;
		deliveries = 0;
		interactions = 0;
		roadModel.queries = 0;
	}
	
	public Result run(long maxRuntime){
		long startTime = System.currentTimeMillis();
		
		while(advanced < ticks){
			if(System.currentTimeMillis() - startTime > maxRuntime) return null;
			sim.advanceTick();
			advanced++;
		}
		
		long endTime = System.currentTimeMillis();
		
		sim.shutdown();
		//System.out.println("speed: " + speed + "  interactions: " + ((interactions / ticks) / nrTrucks));
		return new Result(pickups, deliveries, (interactions / ticks) / nrTrucks, endTime - startTime, roadModel.queries);
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

	public void init(){
		init(0);
	}
	
	public void init(int blocks){
		init(blocks, true);
	}
	
	public void init(int blocks, boolean warmup) {
		double z = Math.max(100, Math.sqrt(nrTrucks) * 50);
		roadModel = new PlaneRoadModel(new Point(0, 0), new Point(z, z), false, 100, blocks);
		rect = roadModel.getViewRect();
		InteractionModel interModel = new InteractionModel();
		PdpModel pdpModel = new PdpModel(new LiberalPolicy(), range, this);
		
		sim.registerModel(roadModel);
		sim.registerModel(interModel);
		sim.registerModel(pdpModel);
		
		registerModels();
		
		if(warmup)
			sim.configureWithWarmup();
		else
			sim.configure();
		
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
		
		int xMin = (int) Math.max(rect.xMin, from.x - 45) + 1;
		int xMax = (int) Math.min(rect.xMax, from.x + 45) - 1;
		
		int yMin = (int) Math.max(rect.yMin, from.y - 45) + 1;
		int yMax = (int) Math.min(rect.yMax, from.y + 45) - 1;
		
		//System.out.println(xMin + " " + xMax + "," + yMin + " " + yMax);
		
		Point to = null;
		do{
		int x = rng.nextInt(xMax - xMin) + xMin;
		int y = rng.nextInt(yMax - yMin) + yMin;
		
		to = new Point(x, y);
		}while(Point.distance(from, to) > 45);
		//System.out.println("rect: " + rect);
		//System.out.println("x " + x + " y " + y);
		
		final Parcel parcel =
				new Parcel(from, to, STEP*2, TimeWindow.ALWAYS, STEP*2, TimeWindow.ALWAYS, 1);
	
		registerParcel(parcel);
	}
	
	abstract protected void registerModels();
	abstract protected void registerTruck(Point pos, double speed, int cap);
	abstract protected void registerParcel(Parcel p);

	public static Scenario makeScenario(int scenarioNr, int seed, AgentsPolicy policy, double speed, int ticks, int cars, double proportion,
			double packageRadius, double gradientRadius, double broadcastRadius){
		switch (scenarioNr) {
		case 0:
			return new NaiveScenario(seed, policy, speed, ticks, cars,
					proportion, packageRadius);
		case 1:
			return new GradientScenario(seed, policy, speed, ticks, cars,
					proportion, packageRadius, gradientRadius);
		case 2:
			return new ContractScenario(seed, policy, speed, ticks, cars,
					proportion, packageRadius, broadcastRadius);
		default:
			throw new IllegalArgumentException();
		}
	}

	public static final class Result{
		public final int pickups;
		public final int deliveries;
		public final double interactionRate;
		public final long runtime;
		public final long queriesPerformed;
		
		public Result(int pickups, int deliveries, double interactionRate, long runtime, long queries){
			this.pickups = pickups;
			this.deliveries = deliveries;
			this.interactionRate = interactionRate;
			this.runtime = runtime;
			this.queriesPerformed = queries;
		}
	}
}