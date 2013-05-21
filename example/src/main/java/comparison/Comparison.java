package comparison;

import gradient.GradientScenario;
import naive.NaiveScenario;
import rinde.sim.core.simulation.policies.AgentsPolicy;
import rinde.sim.core.simulation.policies.agents.ModPoolBatch2;
import rinde.sim.core.simulation.policies.agents.SingleThreaded;

import comparison.Scenario.Result;

import contractnet.ContractScenario;

public class Comparison {

	public static final double FIND_PACKAGE_RADIUS = 50;
	public static final double GRADIENT_RADIUS = 100;
	public static final double BROADCAST_RADIUS = 100;
	
	public static void main(String[] args) {
		int seed = 26;
		int speed = 1;
		int ticks = 500;
		int cars = 2;
		int proportion = 5;
		
		long time = System.currentTimeMillis();
		
		Scenario s = makeScenario(2, seed, new SingleThreaded(), speed, ticks, cars, proportion);
		s.init(0);
		s.runGUI();
		Result r = s.run();
		
		System.out.println("time: " + (System.currentTimeMillis() - time));
		System.out.println("RESULT: " + r.deliveries + " " + r.pickups + " -> " + r.interactionRate);

		time = System.currentTimeMillis();
		s = makeScenario(2, seed, new ModPoolBatch2(10, 2), speed, ticks, cars, proportion);
		s.init(40);
		r = s.run();
		System.out.println("time: " + (System.currentTimeMillis() - time));
		System.out.println("RESULT: " + r.deliveries + " " + r.pickups + " -> " + r.interactionRate);
	}
	
	private static Scenario makeScenario(int nr, 
			int seed, AgentsPolicy policy, int speed, int ticks, int cars, int proportion){
		switch (nr) {
		case 0:
			return new NaiveScenario(seed, policy, speed, ticks, cars,
					proportion, FIND_PACKAGE_RADIUS);
		case 1:
			return new GradientScenario(seed, policy, speed, ticks, cars,
					proportion, FIND_PACKAGE_RADIUS, GRADIENT_RADIUS);
		case 2:
			return new ContractScenario(seed, policy, speed, ticks, cars,
					proportion, FIND_PACKAGE_RADIUS, BROADCAST_RADIUS);
		default:
			throw new IllegalArgumentException();
		}
	}
}
