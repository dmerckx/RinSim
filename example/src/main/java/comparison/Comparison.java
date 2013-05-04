package comparison;

import gradient.FieldTruck;
import rinde.sim.core.simulation.policies.AgentsPolicy;
import rinde.sim.core.simulation.policies.agents.areas.Areas;

public class Comparison {
	
	public static void main(String[] args) {
		int seed = 18;
		AgentsPolicy policy = new Areas(5, 4, 4);
		int speed = 500;
		int ticks = 500;
		int cars = 200;
		int proportion = 10;
		
		Scenario s = makeScenario(0, seed, policy, speed, ticks, cars, proportion);
		s.init();
		Result r = s.run();
		
		System.out.println("RESULT: " + FieldTruck.DELIVERIES + " " + FieldTruck.SEARCHING);
	}
	
	public static final int NR_POLICIES = 1;
	
	private static Scenario makeScenario(int nr, 
			int seed, AgentsPolicy policy, int speed, int ticks, int cars, int proportion){
		switch (nr) {
		case 0:
			return new NaiveScenario(seed, policy, speed, ticks, cars, proportion);
		case 1:
			return new GradientScenario(seed, policy, speed, ticks, cars, proportion);
		default:
			throw new IllegalArgumentException();
		}
	}
}
