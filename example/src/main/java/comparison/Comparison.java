package comparison;

import gradient.GradientScenario;
import naive.NaiveScenario;
import rinde.sim.core.simulation.policies.AgentsPolicy;
import rinde.sim.core.simulation.policies.agents.SingleThreaded;
import contractnet.ContractScenario;

public class Comparison {
	
	public static final double BROADCAST_RADIUS = 100;
	
	public static void main(String[] args) {
		int seed = 18;
		AgentsPolicy policy = null;
		int speed = 1;
		int ticks = 500;
		int cars = 3;
		int proportion = 5;
		
		Scenario s = makeScenario(1, seed, policy, speed, ticks, cars, proportion);
		s.init();
		s.runGUI();
		Result r = s.run();
		
		System.out.println("RESULT: " + r.deliveries + " " + r.pickups + " -> " + r.interactionRate);
	}
	
	public static final int NR_POLICIES = 1;
	
	private static Scenario makeScenario(int nr, 
			int seed, AgentsPolicy policy, int speed, int ticks, int cars, int proportion){
		switch (nr) {
		case 0:
			return new NaiveScenario(seed, policy, speed, ticks, cars, proportion);
		case 1:
			return new GradientScenario(seed, policy, speed, ticks, cars, proportion);
		case 2:
			return new ContractScenario(seed, policy, speed, ticks, cars, proportion, BROADCAST_RADIUS);
		default:
			throw new IllegalArgumentException();
		}
	}
}
