package blocksize;

import plots.Standards;
import rinde.sim.core.simulation.policies.AgentsPolicy;
import rinde.sim.core.simulation.policies.agents.SingleThreaded;

import comparison.Scenario;

public class BlockSizeTableNaive2 extends BlockSizeAbstr{

	
	public static void main(String[] args) {
		MAX_TIME = 15000;
		BASE_TICKS = 5000 * 100;
		AGENTS = new int[AMOUNT];
		TICKS = new int[AMOUNT];
		for(int i = 0; i < AMOUNT; i++){
			AGENTS[i] = (int) Math.pow(/*5*/ 10 + i*5, 2);
			TICKS[i] = BASE_TICKS / AGENTS[i];
		}
		BlockSizeTableNaive2 b = new BlockSizeTableNaive2();
		
		b.warmup();
		b.runScenario();
	}
	
	private BlockSizeTableNaive2() {
		// TODO Auto-generated constructor stub
	}
	
	protected Scenario getScenario(int ticks, int trucks){
		AgentsPolicy policy = new SingleThreaded();
		
		Scenario s = new BlocksizeScenario(rng.nextInt(), policy, Standards.SPEED,
				ticks, trucks, Standards.PROPORTION, Standards.FIND_PACKAGE_RADIUS);
		return s;
	}
}
