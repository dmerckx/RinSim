package comparison;

import gradient.GradientScenario;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import naive.NaiveScenario;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;

import rinde.sim.core.simulation.policies.AgentsPolicy;
import rinde.sim.core.simulation.policies.agents.SingleThreaded;
import rinde.sim.core.simulation.policies.agents.areas.Areas;
import rinde.sim.core.simulation.policies.agents.areas2.Areas2;
import rinde.sim.core.simulation.policies.execution.ModPoolBatchRecursive;

import comparison.Scenario.Result;

public class ComparisonPlot{
	//public static final String PATH = "/home/dmerckx/Documents/plots2/";
	public static String PATH = "/tmp/"; 
	
	
	private static final int SEED = 13;
	private static RandomGenerator rng;
	
	private static final int[] AGENTS_1 =	new int[]{500, 	1000,	1500,	2000,	2500,	3000};
	private static final int[] AGENTS_2 =	new int[]{50, 	100, 	150, 	200, 	250,	300};
	
	private static final int[] TICKS_1 = 	new int[]{8000, 5000, 4000, 4000, 2000, 2000};
	private static final int[] TICKS_2 = 	new int[]{7000, 4500, 3000, 2000, 1000, 1000}; //mss beter 300 van maken
	
	static {
		if(AGENTS_1.length != TICKS_1.length || TICKS_1.length != TICKS_2.length) throw new IllegalStateException();
	}
	
	private static final int SPEED = 1;
	private static final int PROPORTION = 5;
	
	public static void main(String[] args) throws InterruptedException {
		if(args.length > 1){
			System.out.println(args[0]);
			PATH = args[0];
		}
		
		rng = new MersenneTwister(SEED);
		warmup();
		
		double[][][] results		//agents, scenario_nr, rep_nr, policy_nr
			= new double[AGENTS_1.length][NR_SCENARIOS][NR_POLICIES];

		int seed = rng.nextInt();
		
		for(int a = 0; a < AGENTS_1.length; a++){
		for(int s = 0; s < NR_SCENARIOS; s++){
			System.out.println(" --= SCEN: " + s + " =--");
			for(int p = 0; p < NR_POLICIES; p++){
					AgentsPolicy policy = getPolicy(p, s, a);
				
					if(p==5  || p==6){
						Scenario scenario = makeScenario(s, a, seed, policy);
						scenario.init();
						scenario.run();
					}
					
					//AgentsPolicy policy = getPolicy(p, s, a);
					Scenario scenario = makeScenario(s, a, seed, policy);
					
					scenario.init();
	
					long before = System.currentTimeMillis();
					Result dataRes = scenario.run();
					long result = System.currentTimeMillis() - before;
					
					scenario.close();
					
					results[a][s][p] = result;
					
					System.out.println("a:" + a + " s:" + s + " p:" + p + " " + result
							+ " |pick" + dataRes.pickups + "|del" + dataRes.deliveries + "|inter" + dataRes.interactionRate);
				}
			}		
		}
			
		try {
			for(int s = 0; s < NR_SCENARIOS; s++){
				for(int a = 0; a < AGENTS_1.length; a++){
					FileWriter writer = new FileWriter(new File(PATH +
							"COMPARISON" + "a" + a + "s" + s), true);
					for(int p = 0; p < NR_POLICIES; p++){
						//agent_nr, scenario_nr, rep_nr, policy_nr
						writer.write(results[a][s][p] + " ");
					}
					writer.write("\r\n");
					writer.close();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		System.gc();
		Thread.sleep(3000);
	}
	
	private static final int NR_SCENARIOS = 2;
	private static Scenario makeScenario(int scenarioNr, int agentsNr, int seed, AgentsPolicy policy){
		switch (scenarioNr) {
		case 0:
			return new NaiveScenario(seed, policy, SPEED, TICKS_1[agentsNr], AGENTS_1[agentsNr], PROPORTION);
		case 1:
			return new GradientScenario(seed, policy, SPEED, TICKS_2[agentsNr], AGENTS_2[agentsNr], PROPORTION);
		default:
			throw new IllegalArgumentException();
		}
	}
	
	private static void warmup(){
		System.err.println("Warmup is disabled!");
		System.out.println("Warming up..");
		Scenario scen = makeScenario(1, 0, SEED, getPolicy(1, 1, 0));
		scen.init();
		scen.run();
		/*for(int s = 0; s < NR_SCENARIOS; s++){
			for(int p = 0; p < NR_POLICIES; p++){
				Scenario scen = makeScenario(s, SEED, getPolicy(p, s));
				scen.init();
				scen.run();
			}
		}*/
		System.out.println("Warmup completed..");
	}
	
	private static final int NR_POLICIES = 5;
	public static AgentsPolicy getPolicy(int policyNr, int scenarioNr, int agentNr){
		switch(scenarioNr){
		case 0:	//Naive 
			switch(policyNr){
				case 0: return new SingleThreaded();
				case 1: return new ModPoolBatchRecursive(new int[]{65, 100, 120, 120, 120, 120}[agentNr],1);
				case 2: return new ModPoolBatchRecursive(new int[]{55, 85, 120, 120, 120, 120}[agentNr],3);
				case 3: return new ModPoolBatchRecursive(new int[]{50, 75, 90, 100, 100, 100}[agentNr],7);
				case 4: return new ModPoolBatchRecursive(new int[]{50, 75, 90, 100, 100, 100}[agentNr],15);
				case 5: return new Areas(20, 8, 6);
				case 6: return new Areas2(20, 8, 4);
				default:
					throw new IllegalArgumentException("Unknown policy nr");
			}
		case 1: //Gradient field
			switch(policyNr){
				case 0: return new SingleThreaded();
				case 1: return new ModPoolBatchRecursive(new int[]{4, 5, 4, 3, 3, 3}[agentNr],1);
				case 2:
					/*if(agentNr == 1)*/ return new ModPoolBatchRecursive(4,3);
					//else return new ModPoolSingle(3);
				case 3:
					/*if(agentNr == 1)*/ return new ModPoolBatchRecursive(4,7);
					//else return new ModPoolSingle(7);
				case 4: return new ModPoolBatchRecursive(4,15);
				case 5: return new Areas(3, 8, 6);
				case 6: return new Areas2(3, 8, 4);
				default:
					throw new IllegalArgumentException("Unknown policy nr");
			}
		default:
			throw new IllegalArgumentException("Unknown scenario nr");
		}
	}
}

