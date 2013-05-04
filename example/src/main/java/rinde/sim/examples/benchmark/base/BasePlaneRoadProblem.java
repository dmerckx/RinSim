package rinde.sim.examples.benchmark.base;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;

import rinde.sim.core.graph.Point;
import rinde.sim.core.model.road.PlaneRoadModel;
import rinde.sim.core.model.road.RoadModel;
import rinde.sim.core.model.road.users.MovingRoadData;
import rinde.sim.core.simulation.Simulator;
import rinde.sim.core.simulation.policies.AgentsPolicy;

public class BasePlaneRoadProblem{
	private static final int STEP = 1;
	
	private Simulator sim;
	
	private final int ticks;
	private final int agents;
	private final RandomGenerator rng;
	
	public BasePlaneRoadProblem(long seed, AgentsPolicy policy, int ticks, int agents) {
		this.sim = new Simulator(STEP, seed, policy);
		
		this.ticks = ticks;
		this.agents = agents;
		this.rng = new MersenneTwister(seed);
	}
	
	public void run(){
		while(sim.getCurrentTime() < ticks * STEP){
			sim.advanceTick();
		}
	}

	public void init(){
		final RoadModel roadModel = new PlaneRoadModel(new Point(0, 0), new Point(100,100), false, 100);
		
		sim.registerModel(roadModel);
		sim.configure();
		
		for (int i = 0; i < agents; i++) {
			sim.registerUser(
				new BaseRoadUser(),
				new MovingRoadData() {
					@Override
					public Point getStartPosition() {
						return roadModel.getRandomPosition(rng);
					}
					@Override
					public double getInitialSpeed() {
						return 1;
					}
				});
		}
	}
	
	public void close(){
		sim.shutdown();
	}
}
