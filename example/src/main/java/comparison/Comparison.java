package comparison;

import gradient.GradientScenario;
import gradient2.GradientScenario2;
import naive.NaiveScenario;
import plots.Standards;
import rinde.sim.core.simulation.policies.AgentsPolicy;
import rinde.sim.core.simulation.policies.Policies;

import comparison.Scenario.Result;

import contractnet.ContractScenario;

public class Comparison {
	private static int seed = 29;
	private static double speed = Standards.SPEED;
	private static int ticks = 200 ;
	private static int cars = 300;
	private static int proportion = 3;
	private static int scenarioNr = 1;
	
	public static void main(String[] args) {
		if(args.length >= 1) ticks = Integer.parseInt(args[0]);
		if(args.length >= 2) cars = Integer.parseInt(args[1]);
		int blocksize = args.length >= 3? Integer.parseInt(args[2]) : 1;
		Scenario s = null; Result r = null;
		
		System.out.println(seed);
		makeAndRunQuiet(Policies.getModPool(4, 1, true));
		
		makeAndRun(Policies.getModPool(4, 1, true));
		makeAndRun(Policies.getModPool(4, 5, true));
		makeAndRun(Policies.getAdaptive(4));
		
		
		/*for(int i = 1; i <= 8; i = i*2){
			//FieldTruck2.mode = 0;
			makeAndRun(Policies.getModPool(i, 1, true));
			//FieldTruck2.mode = 1;
			makeAndRun(Policies.getModPool(i, 1, true));
			//FieldTruck2.mode = 2;
			makeAndRun(Policies.getModPool(i, 1, true));
		}*/
	}
	
	private static Scenario makeScenario(AgentsPolicy policy){
		switch (scenarioNr) {
		case 0:
			return new NaiveScenario(seed, policy, speed, ticks, cars,
					proportion, Standards.FIND_PACKAGE_RADIUS);
		case 1:
			return new GradientScenario(seed, policy, speed, ticks, cars,
					proportion, Standards.FIND_PACKAGE_RADIUS, Standards.GRADIENT_RADIUS);
		case 2:
			return new ContractScenario(seed, policy, speed, ticks, cars,
					proportion, Standards.FIND_PACKAGE_RADIUS, Standards.BROADCAST_RADIUS);
		case 3:
			return new GradientScenario2(seed, policy, speed, ticks, cars,
					proportion, Standards.FIND_PACKAGE_RADIUS);
		default:
			throw new IllegalArgumentException();
		}
	}
	
	private static void makeAndRunQuiet(AgentsPolicy policy){
		makeAndRun(policy, Standards.getBlocks(cars), true);
	}
	
	private static void makeAndRun(AgentsPolicy policy){
		makeAndRun(policy, Standards.getBlocks(cars), false);
	}
	
	private static void makeAndRun(AgentsPolicy policy, int blocks){
		makeAndRun(policy, blocks, false);
	}
	
	private static void makeAndRun(AgentsPolicy policy, int blocks, boolean quiet){
		Scenario s = makeScenario(policy);
		s.init(blocks);
		Result r = s.run();
		if(!quiet) System.out.println(policy + " : " + r.runtime + " | " + r.deliveries + "/" + r.pickups + " | blocksize: " + blocks);
		else System.out.println(".");
	} 
}


/*System.out.println("new version: " + Standards.getBlocks(cars));
s = makeScenario(scenario, seed, Policies.getModPool(4, 1, true), speed, ticks, cars, proportion);
s.init(0);
r = s.run();

FieldTruck2.mode = 0;
System.out.println("working on it");
s = makeScenario(scenario, seed, Policies.getSingleThreaded(), speed, ticks, cars, proportion);
s.init(0);
r = s.run();
System.out.println("S RESULT: " + r.runtime + " | " + r.deliveries + " " + r.pickups + " -> " + r.interactionRate);

FieldTruck2.mode = 1;
s = makeScenario(scenario, seed, Policies.getSingleThreaded(), speed, ticks, cars, proportion);
s.init(0);
r = s.run();
System.out.println("S RESULT: " + r.runtime + " | " + r.deliveries + " " + r.pickups + " -> " + r.interactionRate);

FieldTruck2.mode = 2;
s = makeScenario(scenario, seed, Policies.getSingleThreaded(), speed, ticks, cars, proportion);
s.init(0);
r = s.run();
System.out.println("S RESULT: " + r.runtime + " | " + r.deliveries + " " + r.pickups + " -> " + r.interactionRate);

//2
FieldTruck2.mode = 0;
s = makeScenario(scenario, seed, Policies.getModPool(2, 1, true), speed, ticks, cars, proportion);
s.init(0);
r = s.run();
System.out.println("2M RESULT: " + r.runtime + " | " + r.deliveries + " " + r.pickups + " -> " + r.interactionRate);


FieldTruck2.mode = 1;
s = makeScenario(scenario, seed, Policies.getModPool(2, 1, true), speed, ticks, cars, proportion);
s.init(0);
r = s.run();
System.out.println("2M RESULT: " + r.runtime + " | " + r.deliveries + " " + r.pickups + " -> " + r.interactionRate);


FieldTruck2.mode = 2;
s = makeScenario(scenario, seed, Policies.getModPool(2, 1, true), speed, ticks, cars, proportion);
s.init(0);
r = s.run();
System.out.println("2M RESULT: " + r.runtime + " | " + r.deliveries + " " + r.pickups + " -> " + r.interactionRate);

//4
FieldTruck2.mode = 0;
s = makeScenario(scenario, seed, Policies.getModPool(4, 1, true), speed, ticks, cars, proportion);
s.init(0);
r = s.run();
System.out.println("4M RESULT: " + r.runtime + " | " + r.deliveries + " " + r.pickups + " -> " + r.interactionRate);	

FieldTruck2.mode = 1;
s = makeScenario(scenario, seed, Policies.getModPool(4, 1, true), speed, ticks, cars, proportion);
s.init(0);
r = s.run();
System.out.println("4M RESULT: " + r.runtime + " | " + r.deliveries + " " + r.pickups + " -> " + r.interactionRate);	

FieldTruck2.mode = 2;
s = makeScenario(scenario, seed, Policies.getModPool(4, 1, true), speed, ticks, cars, proportion);
s.init(0);
r = s.run();
System.out.println("4M RESULT: " + r.runtime + " | " + r.deliveries + " " + r.pickups + " -> " + r.interactionRate);	

//8
FieldTruck2.mode = 0;
s = makeScenario(scenario, seed, Policies.getModPool(8, 1, true), speed, ticks, cars, proportion);
s.init(0);
r = s.run();
System.out.println("8M RESULT: " + r.runtime + " | " + r.deliveries + " " + r.pickups + " -> " + r.interactionRate);	

FieldTruck2.mode = 1;
s = makeScenario(scenario, seed, Policies.getModPool(8, 1, true), speed, ticks, cars, proportion);
s.init(0);
r = s.run();
System.out.println("8M RESULT: " + r.runtime + " | " + r.deliveries + " " + r.pickups + " -> " + r.interactionRate);	

FieldTruck2.mode = 2;
s = makeScenario(scenario, seed, Policies.getModPool(8, 1, true), speed, ticks, cars, proportion);
s.init(0);
r = s.run();
System.out.println("8M RESULT: " + r.runtime + " | " + r.deliveries + " " + r.pickups + " -> " + r.interactionRate);	

//12
FieldTruck2.mode = 0;
s = makeScenario(scenario, seed, Policies.getModPool(12, 1, true), speed, ticks, cars, proportion);
s.init(0);
r = s.run();
System.out.println("12M RESULT: " + r.runtime + " | " + r.deliveries + " " + r.pickups + " -> " + r.interactionRate);

FieldTruck2.mode = 1;
s = makeScenario(scenario, seed, Policies.getModPool(12, 1, true), speed, ticks, cars, proportion);
s.init(0);
r = s.run();
System.out.println("12M RESULT: " + r.runtime + " | " + r.deliveries + " " + r.pickups + " -> " + r.interactionRate);

FieldTruck2.mode = 2;
s = makeScenario(scenario, seed, Policies.getModPool(12, 1, true), speed, ticks, cars, proportion);
s.init(0);
r = s.run();
System.out.println("12M RESULT: " + r.runtime + " | " + r.deliveries + " " + r.pickups + " -> " + r.interactionRate);*/