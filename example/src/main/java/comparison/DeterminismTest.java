package comparison;

import gradient.GradientScenario;
import naive.NaiveScenario;
import naive.NaiveTruck;
import rinde.sim.core.simulation.policies.AgentsPolicy;
import rinde.sim.core.simulation.policies.agents.MultiThreaded;
import rinde.sim.core.simulation.policies.agents.SingleThreaded;
import rinde.sim.core.simulation.policies.execution.BatchExe;

import comparison.Scenario.Result;

import contractnet.ContractScenario;

public class DeterminismTest {
	public static final double FIND_PACKAGE_RADIUS = 100;
	public static final double GRADIENT_RADIUS = 100;
	public static final double BROADCAST_RADIUS = 100;
	
	public static final double NR_TRIES = 5;
	
	public static void main(String[] args) {
		int seed = 26;
		int speed = 2;
		int ticks = 1000;
		int cars = 100;
		double proportion = 3;
		int blocks = 0;
		
		int policyNr = 0;
		
		int scenarioNr = 1;
		int avgDeliveries = 0;
		for(int i = 0; i < NR_TRIES; i++){
			Result r = run(scenarioNr, seed++, policyNr, speed, ticks, cars, proportion, blocks);
			System.out.println(r.pickups + " " + r.deliveries);
			avgDeliveries += r.deliveries;
		}
		System.out.println("avg: " + (avgDeliveries / NR_TRIES));
		
		scenarioNr = 0;
		avgDeliveries = 0;
		for(int i = 0; i < NR_TRIES; i++){
			Result r = run(scenarioNr, seed++, policyNr, speed, ticks, cars, proportion, blocks);
			//System.out.println(r.pickups + " " + r.deliveries);
			avgDeliveries += r.deliveries;
		}
		
		System.out.println("Average pickup time: " + NaiveTruck.getAvgTimeToPickup());
		System.out.println("Average delivery time: " + NaiveTruck.getAvgTimeToDeliver());
		
		System.out.println("avg naive: " + (avgDeliveries / NR_TRIES));
	}
	
	public static Result run(int scenarioNr, int seed, int policyNr, int speed, int ticks, int cars, double proportion, int blocks){
		AgentsPolicy policy = getPolicy(policyNr);
		Scenario s = makeScenario(scenarioNr, seed, policy, speed, ticks, cars, proportion);
		s.init(blocks);
		//s.runGUI();
		return s.run();
	}

	public static final int NR_OF_POLICIES = 2;
	private static AgentsPolicy getPolicy(int policyNr){
		switch (policyNr) {
		case 0:
			return new SingleThreaded();
		case 1:
			return new MultiThreaded(new BatchExe(10), 5);
		default:
			throw new IllegalArgumentException();
		}
	}
	
	private static Scenario makeScenario(int scenarioNr, 
			int seed, AgentsPolicy policy, int speed, int ticks, int cars, double proportion){
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
