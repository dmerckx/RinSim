package rinde.sim.examples.benchmark.base;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;

import rinde.sim.core.graph.Graph;
import rinde.sim.core.graph.MultiAttributeData;
import rinde.sim.core.graph.Point;
import rinde.sim.core.model.road.GraphRoadModel;
import rinde.sim.core.model.road.RoadModel;
import rinde.sim.core.model.road.users.MovingRoadData;
import rinde.sim.core.simulation.Simulator;
import rinde.sim.core.simulation.policies.TimeUserPolicy;
import rinde.sim.serializers.DotGraphSerializer;
import rinde.sim.serializers.SelfCycleFilter;

public class BaseRoadProblem{
	
	private static final int STEP = 10000;
	private static final String MAP_DIR = "../core/files/maps/leuven-simple.dot";
	
	private Simulator sim;
	
	private final int ticks;
	private final int agents;
	private final RandomGenerator rng;
	
	public BaseRoadProblem(long seed, TimeUserPolicy policy, int ticks, int agents) {
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
		try {
			Graph<MultiAttributeData> graph = DotGraphSerializer
					.getMultiAttributeGraphSerializer(new SelfCycleFilter()).read(MAP_DIR);
			final RoadModel roadModel = new GraphRoadModel(graph);
			
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
							return 1000;
						}
					});
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	public void close(){
		sim.shutdown();
	}
}
