package batchsize.adaptive;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import batchsize.BSTableNaive;

import naive.NaiveScenario;
import plots.Standards;
import rinde.sim.core.simulation.policies.AgentsPolicy;

import comparison.Scenario;

public class BSTableAdaptiveNaive extends BSAdaptiveAbstr{
	
	public static void main(String[] args) {
		if(args.length >= 1){
			SAMPLES = Integer.parseInt(args[0]);
		}
		
		CORES = 4;
		
		String res = new BSTableAdaptiveNaive(
				BSTableNaive.AGENTS,
				BSTableNaive.BASE_TICKS).run();
		
		try {
			FileWriter writer = new FileWriter(new File(PATH + "BATCHSIZEADAPTIVE_NAIVE"));
			writer.write(res);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	private BSTableAdaptiveNaive(int[] agents, int baseTicks) {
		super(agents, baseTicks);
	}

	@Override
	protected Scenario getScenario(int ticks, int trucks, AgentsPolicy policy) {
		return new NaiveScenario(rng.nextInt(), policy, Standards.SPEED,
				ticks, trucks, Standards.PROPORTION, Standards.FIND_PACKAGE_RADIUS);
	}

}
