package benchmark;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;

import rinde.sim.core.simulation.policies.AgentsPolicy;
import rinde.sim.core.simulation.policies.agents.SingleThreaded;
import rinde.sim.core.simulation.policies.execution.ModPoolBatchRecursive;

public class InteractionsPlot {

	//public static final String PATH = "/home/dmerckx/Documents/plots2/";
	public static String PATH = "/tmp/";
	
	public static final int AGENT_EVALS = 100000000;
	
	private static final int SEED = 13;
	private static RandomGenerator rng;


	private static final int[] AGENTS = 
			new int[]{2000};
	private static final int[] LOAD =
			new int[]{256};
	private static final int[] INTERACTIONS =
			new int[]{0, 2, 4, 7, 10, 15, 20, 25, 35, 50, 75, 100};
	
	public static void main(String[] args) throws InterruptedException {
		if(args.length == 1){
			System.out.println(args[0]);
			PATH = args[0];
		}
		
		rng = new MersenneTwister(SEED);
		warmup();
		
		double[][][][] results		//agent_nr, load_nr, rep_nr, policy_nr, interactions
			= new double[AGENTS.length][LOAD.length][NR_POLICIES][INTERACTIONS.length];

		for(int a = 0; a < AGENTS.length; a++){
			for(int l = 0; l < LOAD.length; l++){
				for(int i = 0; i < INTERACTIONS.length; i++){
					System.out.println(" --= Interactions: " + INTERACTIONS[i] + " =--");
					for(int p = 0; p < NR_POLICIES; p++){
						double interactions = INTERACTIONS[i];
						int agents = AGENTS[a];
						int load = LOAD[l];
						
						long before = System.currentTimeMillis();
						double result = 
								calc(interactions, p, agents, load, false);
						results[a][l][p][i] = result;
						
						System.out.println("a:" + agents + " l:" + load + " p:" + p + " i:" + interactions + " " + result + " | " + (System.currentTimeMillis()-before));
						//System.out.println(TestUser.NR);
						//TestUser.NR = 0;
					}
				}
			}		
		}
		
		try {
			for(int a = 0; a < AGENTS.length; a++){
				for(int l = 0; l < LOAD.length; l++){
					for(int p = 0; p < NR_POLICIES; p++){
					FileWriter writer = new FileWriter(
								new File(PATH + "INTERACTIONS"
										+ "a" + a
										+ "l" + l
										+ "p" + p), true);
					
						for(int i = 0; i < INTERACTIONS.length; i++){
							//agent_nr, load_nr, rep_nr, policy_nr, interactions
							writer.write(results[a][l][p][i] + " ");
						}
						writer.write("\r\n");
						writer.close();	
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static double calc(double interactions, int policyNr, int agents, int load, boolean warmup){
		double result = 0;
		
		if(getPolicy(policyNr) == null) return 0;
		
		TestProblem problem = new TestProblem(			
				load,
				interactions / 100.0d,
				rng.nextLong(),
				getPolicy(policyNr),
				(int) Math.floor(1.0d * AGENT_EVALS / agents / load),
				agents);
		problem.init(warmup);
			
		long before = System.currentTimeMillis();
		problem.run();
		result = System.currentTimeMillis() - before;
		problem.close();
		
		return result;
	}
	
	private static void warmup(){
		TestProblem problem = new TestProblem(			
				1,
				0.00,
				15,
				getPolicy(0),
				500,
				200);
		problem.init(true);
		System.out.println("Warming up..");
		problem.run();
		problem.close();
		System.out.println("Warmup 1/2 completed.");
		for(int p = 0; p < NR_POLICIES; p++){
			calc(5, p, 500, 16, true);
		}
		
		System.out.println("Warmup completed.");
	}
	
	private static final int NR_POLICIES = 4;
	public static AgentsPolicy getPolicy(int nr){
		switch(nr){
			case 0: return new SingleThreaded();
			case 1: return new ModPoolBatchRecursive(5,1);
			case 2: return new ModPoolBatchRecursive(5,3);
			case 3: return new ModPoolBatchRecursive(5,7);
			default:
				throw new IllegalArgumentException("Unknown policy nr");
		}
	}
}

