package comparison;

import plots.Standards;
import gradient.GradientScenario;
import naive.NaiveScenario;
import rinde.sim.core.simulation.policies.AgentsPolicy;
import rinde.sim.core.simulation.policies.agents.ModPoolBatchRecursive;
import rinde.sim.core.simulation.policies.agents.SingleThreaded;
import rinde.sim.core.simulation.policies.agents.areas.Areas;

import comparison.Scenario.Result;

import contractnet.ContractScenario;

public class Comparison {
	public static void main(String[] args) {
		int seed = 26;
		double speed = Standards.SPEED;
		int ticks = 18000;
		int cars = 1500;
		int proportion = 5;
		
		int scenario = 2;
		Scenario s = null; Result r = null;
		

		/*s = makeScenario(scenario, seed, new SingleThreaded(), speed, ticks, cars, proportion);
		s.init(Standards.getBlocks(cars));
		r = s.run();
		
		s = makeScenario(scenario, seed, new SingleThreaded(), speed, ticks, cars, proportion);
		s.init(Standards.getBlocks(cars));
		r = s.run();
		
		System.out.println("RESULT: " + r.runtime + " | " + r.deliveries + " " + r.pickups + " -> " + r.interactionRate);*/

		s = makeScenario(scenario, seed, new Areas(5, 2, 3), speed, ticks, cars, proportion);
		s.init(Standards.getBlocks(cars));
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
