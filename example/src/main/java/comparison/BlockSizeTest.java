package comparison;

import gradient.GradientScenario;
import naive.NaiveScenario;
import rinde.sim.core.simulation.policies.AgentsPolicy;
import rinde.sim.core.simulation.policies.Policies;
import rinde.sim.core.simulation.policies.agents.MultiThreaded;
import rinde.sim.core.simulation.policies.agents.SingleThreaded;

import comparison.Scenario.Result;

import contractnet.ContractScenario;

public class BlockSizeTest {
	public static final double FIND_PACKAGE_RADIUS = 50;
	public static final double GRADIENT_RADIUS = 100;
	public static final double BROADCAST_RADIUS = 50;
	
	public static final double NR_TRIES = 10;
	
	public static void main(String[] args) {
		int seed = 26;
		int speed = 2;
		int ticks = 1500;
		int cars = 100;
		int proportion = 2;
		int blocks = 3;
		
		int policyNr = 1;
		int scenarioNr = 1;
		
		for(int i = 0; i < NR_TRIES; i++){
			Result r = run(scenarioNr, seed, policyNr, speed, ticks, cars, proportion, blocks);
			System.out.println(r.pickups + " " + r.deliveries);
		}
	}
	
	public static Result run(int scenarioNr, int seed, int policyNr, int speed, int ticks, int cars, int proportion, int blocks){
		AgentsPolicy policy = getPolicy(policyNr);
		Scenario s = makeScenario(scenarioNr, seed, policy, speed, ticks, cars, proportion);
		s.init(blocks);
		return s.run();
	}

	public static final int NR_OF_POLICIES = 2;
	private static AgentsPolicy getPolicy(int policyNr){
		switch (policyNr) {
		case 0:
			return new SingleThreaded();
		case 1:
			return Policies.getModPool(2, 10, true);
		default:
			throw new IllegalArgumentException();
		}
	}
	
	private static Scenario makeScenario(int scenarioNr, 
			int seed, AgentsPolicy policy, int speed, int ticks, int cars, int proportion){
		switch (scenarioNr) {
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