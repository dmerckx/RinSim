package benchmark.old;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;

import benchmark.TestProblem;

import rinde.sim.core.simulation.policies.AgentsPolicy;
import rinde.sim.core.simulation.policies.agents.MultiThreaded;
import rinde.sim.core.simulation.policies.agents.ModPoolSingle;
import rinde.sim.core.simulation.policies.agents.SimplePoolSingle;
import rinde.sim.core.simulation.policies.agents.SimplePoolBatch;
import rinde.sim.core.simulation.policies.agents.SingleThreaded;

public class LoadNoInteractionsPlot {

	public static final int CORES = 4;
	public static final int REPS = 6;
	public static final String PATH = "/tmp/";
	//public static final String PATH = "/home/dmerckx/Documents/plots/";
	
	private static int TICKS = 10000;
	private static int AGENTS = 250;
	private static final double INTERACTIONS = 0.0d;
	private static final int SEED = 13;
	
	private static RandomGenerator rng;
	
	private static final int[] LOADS = new int[]{1, 2, 4, 8, 16, 32, 64, 128, 256};
	

	public static void main(String[] args) {
		rng = new MersenneTwister(SEED);

		TestProblem problem = new TestProblem(			
			50,
			INTERACTIONS,
			rng.nextLong(),
			getPolicy(0),
			1000*2,
			200);
		problem.init();
		System.out.println("Warming up..");
		problem.run();
		problem.close();
		calc(5, REFERENCE_POLICY);
		System.out.println("Warmup completed.");
		
		AGENTS = 250;
		run();

		TICKS /= 2;
		AGENTS = 500;
		run();

		TICKS /= 1.5;
		AGENTS = 750;
		run();

		TICKS /= 1.5;
		AGENTS = 1000;
		run();
	}
	public static void run() {
		System.out.println(" ~~ RUNNING " + AGENTS + " AGENTS ~~ ");
		
		double[][] results = new double[LOADS.length][NR_POLICIES];

		for(int l = 0; l < LOADS.length; l++){
			results[l] = getResults(LOADS[l]);
		}
		 
		try {
			FileWriter writer = new FileWriter(new File(PATH + "LOADSa" + AGENTS));
			for(int l = 0; l < LOADS.length; l++){
				for(int p = 0; p < NR_POLICIES; p++){
					writer.write(results[l][p] + " ");
				}
				writer.write("\r\n");
			}
			writer.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
	
	public static double[] getResults(int load){
		double[] results = new double[NR_POLICIES];
		
		System.out.println("-----Load: " + load + " -----");
		
		double reference = calc(load, REFERENCE_POLICY);

		System.out.println("Reference runtime: " + reference);
		System.out.println(" ");
		
		for(int p = 0; p < NR_POLICIES; p++){
			results[p] = reference / calc(load, p);
			System.out.println(p + ") runtime perc: " + results[p]);
			System.out.println(" ");
		}
		
		System.out.println("----------");
		System.out.println(" ");
		
		return results;
	}
	
	public static double calc(int load, int policyNr){
		double result = 0;
		
		for(int r = 0; r < REPS; r++){
			TestProblem problem = new TestProblem(			
				load,
				INTERACTIONS,
				rng.nextLong(),
				getPolicy(policyNr),
				TICKS/load,
				AGENTS);
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
			case 0: return new SimplePoolSingle(CORES);
			case 1: return new SimplePoolBatch(2, CORES);
			case 2: return new SimplePoolBatch(5, CORES);
			case 3: return new SimplePoolBatch(10, CORES);
			//Custom
			case 4: return new ModPoolSingle(CORES-1);
			case 5: return new MultiThreaded(2,CORES-1);
			case 6: return new MultiThreaded(5,CORES-1);
			case 7: return new MultiThreaded(10,CORES-1);
			case 100: return new SingleThreaded();
			default: throw new IllegalArgumentException("Unknown policy nr");
		}
	}
}
