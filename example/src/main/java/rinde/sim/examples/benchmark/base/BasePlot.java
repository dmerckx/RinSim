package rinde.sim.examples.benchmark.base;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;

import rinde.sim.core.simulation.policies.AgentsPolicy;
import rinde.sim.core.simulation.policies.agents.ModPoolBatch;
import rinde.sim.core.simulation.policies.agents.ModPoolSingle;
import rinde.sim.core.simulation.policies.agents.PoolBatch;
import rinde.sim.core.simulation.policies.agents.PoolSingle;
import rinde.sim.core.simulation.policies.agents.SingleThreaded;

public class BasePlot {

	public static final int CORES = 4;
	public static final int REPS = 6;
	
	public static final String PATH = "/tmp/";
	
	private static final int NR_TICKS = 400000;
	private static final int SEED = 13;
	
	private static RandomGenerator rng;
	
	private static final int[] AGENTS = new int[]{16, 32, 64, 128, 256, 512, 1024, 2048, 4096, 8192, 16384};
	

	public static void main(String[] args) {
		rng = new MersenneTwister(SEED);

		BasePlaneRoadProblem problem = new BasePlaneRoadProblem(
				SEED,
				getPolicy(0),
				3000,
				300);
		problem.init();
		System.out.println("Warming up..");
		problem.run();
		problem.close();
		calc(500, REFERENCE_POLICY);
		System.out.println("Warmup completed.");
		
		run();
	}
	
	public static void run() {
		double[][] results = new double[AGENTS.length][NR_POLICIES];

		for(int a = 0; a < AGENTS.length; a++){
			results[a] = getResults(AGENTS[a]);
		}
		 
		try {
			FileWriter writer = new FileWriter(new File(PATH + "BASE"));
			for(int a = 0; a < AGENTS.length; a++){
				for(int p = 0; p < NR_POLICIES; p++){
					writer.write(results[a][p] + " ");
				}
				writer.write("\r\n");
			}
			writer.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
	
	public static double[] getResults(int agents){
		double[] results = new double[NR_POLICIES];
		
		System.out.println("-----Agents: " + agents + " -----");
		
		double reference = calc(agents, REFERENCE_POLICY);

		System.out.println("Reference runtime: " + reference);
		System.out.println(" ");
		
		for(int p = 0; p < NR_POLICIES; p++){
			results[p] = reference / calc(agents, p);
			System.out.println(p + ") runtime perc: " + results[p]);
			System.out.println(" ");
		}
		
		System.out.println("----------");
		System.out.println(" ");
		
		return results;
	}
	
	public static double calc(int agents, int policyNr){
		double result = 0;
		
		for(int r = 0; r < REPS; r++){
			BaseRoadProblem problem = new BaseRoadProblem(			
				rng.nextLong(),
				getPolicy(policyNr),
				NR_TICKS / agents,
				agents);
			problem.init();
			
			long before = System.currentTimeMillis();
			problem.run();
			result += System.currentTimeMillis() - before;
			
			problem.close();
			//System.out.println("done");
		}
		
		return result / REPS;
	}
	
	private static final int NR_POLICIES = 8;
	private static final int REFERENCE_POLICY = 100;
	public static AgentsPolicy getPolicy(int nr){
		switch(nr){
		case 0: return new PoolSingle(CORES);
		case 1: return new PoolBatch(2, CORES);
		case 2: return new PoolBatch(5, CORES);
		case 3: return new PoolBatch(10, CORES);
		//Custom
		case 4: return new ModPoolSingle(CORES-1);
		case 5: return new ModPoolBatch(2,CORES-1);
		case 6: return new ModPoolBatch(5,CORES-1);
		case 7: return new ModPoolBatch(10,CORES-1);
		case 100: return new SingleThreaded();
		default: throw new IllegalArgumentException("Unknown policy nr");
		}
	}
}
