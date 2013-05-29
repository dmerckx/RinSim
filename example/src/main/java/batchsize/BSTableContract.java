package batchsize;

import gradient.GradientScenario;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;

import plots.Standards;
import rinde.sim.core.simulation.policies.AgentsPolicy;
import rinde.sim.core.simulation.policies.Policies;

import comparison.Scenario;
import contractnet.ContractScenario;

public class BSTableContract  extends BatchSizeTableAbstr{

	public static final String PATH = "/tmp/";
	
	public static final int[] AGENTS = new int[]{50, 100, 200, 400, 800, 1200, 1500, 2000, 3000};
	public static final int[] BATCHSIZES = new int[]{0, 1, 5, 15, 30, 60, 100, 200, 300, 500};
	
	private static int BASE_TICKS = 10000 * 100;
	
	
	//public static final String[] NAMES = new String[]{"Naive", "GradientField", "ContractNet"};
	
	private static final RandomGenerator rng = new MersenneTwister();
	
	public static void main(String[] args) {
		if(args.length >= 1){
			SAMPLES = Integer.parseInt(args[0]);
		}
		if(args.length >= 2){
			BASE_TICKS = Integer.parseInt(args[1]) * 100;
		}
		
		CORES = 4; //TODO: test with 8
		
		String res = new BSTableContract(AGENTS, BATCHSIZES, BASE_TICKS).run();
		
		try {
			FileWriter writer = new FileWriter(new File(PATH + "BATCHSIZETABLE"));
			writer.write(res);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public BSTableContract(int[] agents, int[] batchsizes, int baseTicks) {
		super(agents, batchsizes, baseTicks);
	}

	@Override
	protected Scenario getScenario(int ticks, int trucks, int batchSize) {
		AgentsPolicy policy = batchSize == 0 ? Policies.getSingleThreaded(): Policies.getModPool(CORES, batchSize, true);
		
		Scenario s = new ContractScenario(rng.nextInt(), policy, Standards.SPEED,
				ticks, trucks, Standards.PROPORTION, Standards.FIND_PACKAGE_RADIUS, Standards.BROADCAST_RADIUS);
		return s;
	}
	
	
}
