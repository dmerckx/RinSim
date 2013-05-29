package batchsize;

import gradient.GradientScenario;

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

public class BSTableGradient extends BatchSizeTableAbstr{

	public static final String PATH = "/tmp/";
	
	public static final int[] AGENTS = new int[]{50, 100, 150, 200, 300, 500, 700};
	public static final int[] BATCHSIZES = new int[]{0, 1, 2, 4, 6, 8, 12, 15, 20};
	
	private static int BASE_TICKS = 3000 * 100;
	
	
	public static void main(String[] args) {
		if(args.length >= 1){
			SAMPLES = Integer.parseInt(args[0]);
		}
		if(args.length >= 2){
			BASE_TICKS = Integer.parseInt(args[1]) * 100;
		}
		
		CORES = 4; //TODO: test with 8
		
		String res = new BSTableGradient(AGENTS, BATCHSIZES, BASE_TICKS).run();
		
		try {
			FileWriter writer = new FileWriter(new File(PATH + "BATCHSIZETABLE"));
			writer.write(res);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public BSTableGradient(int[] agents, int[] batchsizes, int baseTicks) {
		super(agents, batchsizes, baseTicks);
	}

	@Override
	protected Scenario getScenario(int ticks, int trucks, int batchSize) {
		AgentsPolicy policy = batchSize == 0 ? Policies.getSingleThreaded(): Policies.getModPool(CORES, batchSize, true);
		
		Scenario s = new GradientScenario(rng.nextInt(), policy, Standards.SPEED,
				ticks, trucks, Standards.PROPORTION, Standards.FIND_PACKAGE_RADIUS, Standards.GRADIENT_RADIUS);
		return s;
	}
	
	
}
