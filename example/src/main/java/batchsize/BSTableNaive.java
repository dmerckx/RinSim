package batchsize;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import naive.NaiveScenario;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;

import plots.Standards;
import rinde.sim.core.simulation.policies.AgentsPolicy;
import rinde.sim.core.simulation.policies.Policies;

import comparison.Scenario;

public class BSTableNaive extends BatchSizeTableAbstr{

	public static final String PATH = "/tmp/";
	
	public static final int[] AGENTS = new int[]{100, 300, 600, 800, 1000, 2000, 3000, 4000, 5000, 6000, 7000, 8000, 9000};
	public static final int[] BATCHSIZES = new int[]{0, 1, 15, 50, 100, 200, 400, 800, 1200};
	
	public static int BASE_TICKS = 20000 * 100;
	
	
	//public static final String[] NAMES = new String[]{"Naive", "GradientField", "ContractNet"};
	
	private static final RandomGenerator rng = new MersenneTwister();
	
	public static void main(String[] args) {
		if(args.length >= 1){
			SAMPLES = Integer.parseInt(args[0]);
		}
		if(args.length >= 2){
			BASE_TICKS = Integer.parseInt(args[1]) * 100;
		}
		
		CORES = 4;
		
		String res = new BSTableNaive(AGENTS, BATCHSIZES, BASE_TICKS).run();
		
		try {
			FileWriter writer = new FileWriter(new File(PATH + "BATCHSIZETABLE_NAIVE"));
			writer.write(res);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public BSTableNaive(int[] agents, int[] batchsizes, int baseTicks) {
		super(agents, batchsizes, baseTicks);
	}

	@Override
	protected Scenario getScenario(int ticks, int trucks, int batchSize) {
		AgentsPolicy policy = batchSize == 0 ? Policies.getSingleThreaded(): Policies.getModPool(CORES, batchSize, true);
		
		Scenario s = new NaiveScenario(rng.nextInt(), policy, Standards.SPEED,
				ticks, trucks, Standards.PROPORTION, Standards.FIND_PACKAGE_RADIUS);
		return s;
	}
	
	
}
