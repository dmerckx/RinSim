package rinde.sim.examples.benchmark.base;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;

import rinde.sim.core.simulation.policies.TimeUserPolicy;
import rinde.sim.core.simulation.policies.parallel.CustomPool;
import rinde.sim.core.simulation.policies.parallel.FastSingleCustomPool;
import rinde.sim.core.simulation.policies.parallel.FastSinglePool;
import rinde.sim.core.simulation.policies.parallel.PBatchTimeUserPolicy;
import rinde.sim.core.simulation.policies.parallel.SingleThreaded;

public class BaseNoInteractionsPlot {

	public static final int CORES = 4;
	public static final int REPS = 1;
	public static final String PATH = "/home/dmerckx/Documents/plots/";
	
	private static final int NR_TICKS = 20000;
	private static final int SEED = 13;
	
	private static RandomGenerator rng;
	
	private static final int[] AGENTS = new int[]{25, 50, 100, 200, 400, 800, 1600};
	

	public static void main(String[] args) {
		rng = new MersenneTwister(SEED);

		/*BaseRoadProblem problem = new BaseRoadProblem(
				SEED,
				getPolicy(0),
				5000,
				300);
		problem.init();
		System.out.println("Warming up..");
		problem.run();
		problem.close();
		calc(500, REFERENCE_POLICY);
		System.out.println("Warmup completed.");*/
		
		calc(1000, REFERENCE_POLICY);
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
				agents < 400? NR_TICKS *2 : NR_TICKS,
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
	
	private static final int NR_POLICIES = 6;
	private static final int REFERENCE_POLICY = 100;
	public static TimeUserPolicy getPolicy(int nr){
		switch(nr){
		case 0: return new FastSinglePool(CORES);
		case 1: return new PBatchTimeUserPolicy(2, CORES);
		case 2: return new PBatchTimeUserPolicy(5, CORES);
		//Custom
		case 3: return new FastSingleCustomPool(1,CORES-1);
		case 4: return new CustomPool(2,CORES-1);
		case 5: return new CustomPool(5,CORES-1);
		case 100: return new SingleThreaded();
		default: throw new IllegalArgumentException("Unknown policy nr");
		}
	}
}
