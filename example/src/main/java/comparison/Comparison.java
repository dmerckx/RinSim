package comparison;

import gradient.GradientScenario;
import naive.NaiveScenario;
import plots.Standards;
import rinde.sim.core.simulation.policies.AgentsPolicy;
import rinde.sim.core.simulation.policies.Policies;

import comparison.Scenario.Result;

import contractnet.ContractScenario;

public class Comparison {
	public static void main(String[] args) {
		int seed = 26;
		double speed = Standards.SPEED;
		int ticks = 3000;
		int cars = 50;
		int proportion = 3;
		
		int scenario = 1;
		Scenario s = null; Result r = null;

		System.out.println(Standards.getBlocks(cars));

		s = makeScenario(scenario, seed, Policies.getSingleThreaded(), speed, ticks, cars, proportion);
		s.init(Standards.getBlocks(cars));
		r = s.run();
		
		System.out.println("working on it");
		
		s = makeScenario(scenario, seed, Policies.getSingleThreaded(), speed, ticks, cars, proportion);
		s.init(Standards.getBlocks(cars));
		r = s.run();
		System.out.println("RESULT: " + r.runtime + " | " + r.deliveries + " " + r.pickups + " -> " + r.interactionRate);
		
		s = makeScenario(scenario, seed, Policies.getModPool(4, 1, true), speed, ticks, cars, proportion);
		s.init(Standards.getBlocks(cars));
		r = s.run();
		System.out.println("RESULT: " + r.runtime + " | " + r.deliveries + " " + r.pickups + " -> " + r.interactionRate);
		
		s = makeScenario(scenario, seed, Policies.getModPool(4, 5, true), speed, ticks, cars, proportion);
		s.init(Standards.getBlocks(cars));
		r = s.run();
		System.out.println("RESULT: " + r.runtime + " | " + r.deliveries + " " + r.pickups + " -> " + r.interactionRate);

		s = makeScenario(scenario, seed, Policies.getModPool(4, 5, true), speed, ticks, cars, proportion);
		s.init(12);
		r = s.run();
		System.out.println("RESULT: " + r.runtime + " | " + r.deliveries + " " + r.pickups + " -> " + r.interactionRate);
		s = makeScenario(scenario, seed, Policies.getModPool(4, 5, true), speed, ticks, cars, proportion);
		s.init(16);
		r = s.run();
		System.out.println("RESULT: " + r.runtime + " | " + r.deliveries + " " + r.pickups + " -> " + r.interactionRate);
		s = makeScenario(scenario, seed, Policies.getModPool(4, 5, true), speed, ticks, cars, proportion);
		s.init(20);
		r = s.run();
		System.out.println("RESULT: " + r.runtime + " | " + r.deliveries + " " + r.pickups + " -> " + r.interactionRate);
	}
	
	private static Scenario makeScenario(int nr, 
			int seed, AgentsPolicy policy, double speed, int ticks, int cars, int proportion){
		switch (nr) {
		case 0:
			return new NaiveScenario(seed, policy, speed, ticks, cars,
					proportion, Standards.FIND_PACKAGE_RADIUS);
		case 1:
			return new GradientScenario(seed, policy, speed, ticks, cars,
					proportion, Standards.FIND_PACKAGE_RADIUS, Standards.GRADIENT_RADIUS);
		case 2:
			return new ContractScenario(seed, policy, speed, ticks, cars,
					proportion, Standards.FIND_PACKAGE_RADIUS, Standards.BROADCAST_RADIUS);
		default:
			throw new IllegalArgumentException();
		}
	}
}
