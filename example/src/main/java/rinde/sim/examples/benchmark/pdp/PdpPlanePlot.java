package rinde.sim.examples.benchmark.pdp;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;

import rinde.sim.core.simulation.policies.AgentsPolicy;
import rinde.sim.core.simulation.policies.agents.MultiThreaded;
import rinde.sim.core.simulation.policies.agents.ModPoolSingle;
import rinde.sim.core.simulation.policies.agents.SimplePoolBatch;
import rinde.sim.core.simulation.policies.agents.SimplePoolSingle;
import rinde.sim.core.simulation.policies.agents.SingleThreaded;

public class PdpPlanePlot {

	public static final int CORES = 4;
	public static final int REPS = 6;
	public static final String PATH = "/tmp/";
	//public static final String PATH = "/home/dmerckx/Documents/plots/";

	private static int AGENTS = 1000;		//The total number of trucks
	private static final int PROPORTION = 4; 	//How many packages per truck
	private static int NR_TICKS = 1000;
	
	private static final int SEED = 13;

	private static final int[] SPEED = new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
	
	private static RandomGenerator rng;

	public static void main(String[] args) {
		rng = new MersenneTwister(SEED);

		PdpPlaneProblem problem = new PdpPlaneProblem(
				SEED,
				getPolicy(0),
				1,
				500,
				200,
				4);
		problem.init();
		System.out.println("Warming up..");
		problem.run();
		problem.close();
		calc(1, REFERENCE_POLICY);
		System.out.println("Warmup completed.");
		
		AGENTS = 500;
		run();

		AGENTS = 1000;
		NR_TICKS /= 2;
		run();

		AGENTS = 2000;
		NR_TICKS /= 2;
		run();
	}
	
	public static void run() {
		double[][] results = new double[SPEED.length][NR_POLICIES];

		for(int s = 0; s < SPEED.length; s++){
			results[s] = getResults(SPEED[s]);
		}
		 
		try {
			FileWriter writer = new FileWriter(new File(PATH + "PDPPLANEa"+AGENTS));
			for(int a = 0; a < SPEED.length; a++){
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
	
	public static double[] getResults(int speed){
		double[] results = new double[NR_POLICIES];
		
		System.out.println("-----Speed: " + speed + " -----");
		
		double reference = calc(speed, REFERENCE_POLICY);

		System.out.println("Reference runtime: " + reference);
		System.out.println(" ");
		
		for(int p = 0; p < NR_POLICIES; p++){
			results[p] = reference / calc(speed, p);
			System.out.println(p + ") runtime perc: " + results[p]);
			System.out.println(" ");
		}
		
		System.out.println("----------");
		System.out.println(" ");
		
		return results;
	}
	
	public static double calc(int speed, int policyNr){
		double result = 0;
		
		for(int r = 0; r < REPS; r++){
			PdpPlaneProblem problem = new PdpPlaneProblem(
					SEED,
					getPolicy(0),
					speed,
					NR_TICKS,
					AGENTS,
					PROPORTION);
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
		case 2: return new SimplePoolBatch(2, CORES);
		case 3: return new SimplePoolBatch(5, CORES);
		case 4: return new SimplePoolBatch(10, CORES);
		//Custom
		case 5: return new ModPoolSingle(CORES-1);
		case 6: return new MultiThreaded(2,CORES-1);
		case 7: return new MultiThreaded(5,CORES-1);
		case 8: return new MultiThreaded(10,CORES-1);
		case 100: return new SingleThreaded();
		default: throw new IllegalArgumentException("Unknown policy nr");
		}
	}
}
