package batchsize.adaptive;

import gradient.GradientScenario;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import plots.Standards;
import rinde.sim.core.simulation.policies.AgentsPolicy;
import batchsize.BSTableGradient;

import comparison.Scenario;

public class BSTableAdaptiveGradient extends BSAdaptiveAbstr{
	
	public static void main(String[] args) {
		if(args.length >= 1){
			SAMPLES = Integer.parseInt(args[0]);
		}
		
		CORES = 4;
		
		String res = new BSTableAdaptiveGradient(
				BSTableGradient.AGENTS,
				BSTableGradient.BASE_TICKS).run();
		
		try {
			FileWriter writer = new FileWriter(new File(PATH + "BATCHSIZEADAPTIVE_GRADIENT"));
			writer.write(res);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	private BSTableAdaptiveGradient(int[] agents, int baseTicks) {
		super(agents, baseTicks);
	}

	@Override
	protected Scenario getScenario(int ticks, int trucks, AgentsPolicy policy) {
		return new GradientScenario(rng.nextInt(), policy, Standards.SPEED,
				ticks, trucks, Standards.PROPORTION, Standards.FIND_PACKAGE_RADIUS, Standards.GRADIENT_RADIUS);
	}

}
