package rinde.sim.examples.benchmark.simple;

import rinde.sim.core.simulation.Simulator;
import rinde.sim.core.simulation.policies.TimeUserPolicy;

public class TestProblem {
	
	/*public static int ticksDone = 0;
	public static int times = 0;
	
	public static synchronized void addTime(long processTime){
		times += processTime;
		ticksDone++;
	}
	
	public static double getAvgTime(){
		return (1.0d * times) / ticksDone;
	}*/
	
	private static final int STEP = 1;
	
	private Simulator sim;
	private int load;
	private double interactions;
	
	private final int ticks;
	private final int agents;
	
	public TestProblem(int load, double interactions, long seed, TimeUserPolicy policy, int ticks, int agents) {
		
		this.sim = new Simulator(STEP, seed, policy);
		this.load = load;
		this.interactions = interactions;
		
		
		this.ticks = ticks;
		this.agents = agents;
	}
	
	public void run(){
		while(sim.getCurrentTime() < ticks){
			sim.advanceTick();
		}
	}

	public void init(){
		
		TestModel model = new TestModel();
		
		sim.registerModel(model);
		sim.configure();
		

		for (int i = 0; i < agents; i++) {
			sim.registerUser(new TestUser(load, interactions));
		}
	}
	
	public void close(){
		sim.shutdown();
	}
}
