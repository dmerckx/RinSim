package batchsize;

import gradient.GradientScenario;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import plots.Standards;
import rinde.sim.core.simulation.policies.AgentsPolicy;
import rinde.sim.core.simulation.policies.Policies;

import comparison.Scenario;

public class BSTableGradient extends BatchSizeTableAbstr{

	public static final String PATH = "/tmp/";
	
	public static final int[] AGENTS = new int[]{50, 100, 150, 200, 300, 500, 700};
	public static final int[] BATCHSIZES = new int[]{0, 1, 4};
	
	private static int BASE_TICKS = 1400 * 100;
	
	
	public static void main(String[] args) {
		CORES = 4; //TODO: test with 8
		
		if(args.length >= 1){
			SAMPLES = Integer.parseInt(args[0]);
		}
		if(args.length >= 2){
			CORES = Integer.parseInt(args[1]);
		}
		if(args.length >= 3){
			BASE_TICKS = Integer.parseInt(args[2]) * 100;
		}
		
		String res = new BSTableGradient(AGENTS, BATCHSIZES, BASE_TICKS).run();
		
		try {
			FileWriter writer = new FileWriter(new File(PATH + "BATCHSIZETABLE_GRADIENT"));
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
