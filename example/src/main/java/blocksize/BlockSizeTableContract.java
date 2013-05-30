package blocksize;

import plots.Standards;
import rinde.sim.core.simulation.policies.AgentsPolicy;
import rinde.sim.core.simulation.policies.agents.SingleThreaded;

import comparison.Scenario;

import contractnet.ContractScenario;

public class BlockSizeTableContract extends BlockSizeAbstr{
	
	public static void main(String[] args) {
		MAX_TIME = 15000;
		BASE_TICKS = 10000 * 100;
		AGENTS = new int[AMOUNT];
		TICKS = new int[AMOUNT];
		for(int i = 0; i < AMOUNT; i++){
			AGENTS[i] = (int) Math.pow(/*5*/ 10 + i*5, 2);
			TICKS[i] = BASE_TICKS / AGENTS[i];
		}
		BlockSizeTableContract b = new BlockSizeTableContract();
		
		b.warmup();
		b.runScenario();
	}
	
	private BlockSizeTableContract() {
		// TODO Auto-generated constructor stub
	}
	
	@Override
	protected Scenario getScenario(int ticks, int trucks) {
		AgentsPolicy policy = new SingleThreaded();
		
		Scenario s = new ContractScenario(rng.nextInt(), policy, Standards.SPEED,
				ticks, trucks, Standards.PROPORTION, Standards.FIND_PACKAGE_RADIUS, Standards.BROADCAST_RADIUS);
		return s;
	}

}
