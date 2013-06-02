package batchsize.adaptive;

import gradient.GradientScenario;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import plots.Standards;
import rinde.sim.core.simulation.policies.AgentsPolicy;
import rinde.sim.core.simulation.policies.Policies;
import batchsize.BSTableContract;

import comparison.Scenario;
import contractnet.ContractScenario;

public class BSTableAdaptiveContract extends BSAdaptiveAbstr{
	
	public static void main(String[] args) {
		if(args.length >= 1){
			SAMPLES = Integer.parseInt(args[0]);
		}
		
		CORES = 4;
		
		String res = new BSTableAdaptiveContract(
				BSTableContract.AGENTS,
				BSTableContract.BASE_TICKS).run();
		
		try {
			FileWriter writer = new FileWriter(new File(PATH + "BATCHSIZEADAPTIVE_CONTRACT"));
			writer.write(res);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	private BSTableAdaptiveContract(int[] agents, int baseTicks) {
		super(agents, baseTicks);
	}

	@Override
	protected Scenario getScenario(int ticks, int trucks, AgentsPolicy policy) {
		policy = Policies.getModPool(4, 50, true);
		return new ContractScenario(rng.nextInt(), policy, Standards.SPEED,
				ticks, trucks, Standards.PROPORTION, Standards.FIND_PACKAGE_RADIUS, Standards.BROADCAST_RADIUS);
	}

}
