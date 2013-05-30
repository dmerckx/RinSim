package blocksize;

import gradient.GradientScenario;
import plots.Standards;
import rinde.sim.core.simulation.policies.AgentsPolicy;
import rinde.sim.core.simulation.policies.agents.SingleThreaded;

import comparison.Scenario;

public class BlockSizeTableGradient extends BlockSizeAbstr{
	
	public static void main(String[] args) {
		MAX_TIME = 25000;
		BASE_TICKS = 2500 * 100;
		AGENTS = new int[AMOUNT];
		TICKS = new int[AMOUNT];
		for(int i = 0; i < AMOUNT; i++){
			AGENTS[i] = (int) Math.pow(/*5*/ 10 + i*5, 2);
			TICKS[i] = BASE_TICKS / AGENTS[i];
		}
		BlockSizeTableGradient b = new BlockSizeTableGradient();
		
		b.warmup();
		b.runScenario();
	}
	
	private BlockSizeTableGradient() {
		// TODO Auto-generated constructor stub
	}
	
	@Override
	protected Scenario getScenario(int ticks, int trucks) {
		AgentsPolicy policy = new SingleThreaded();
		
		Scenario s = new GradientScenario(rng.nextInt(), policy, Standards.SPEED,
				ticks, trucks, Standards.PROPORTION, Standards.FIND_PACKAGE_RADIUS, Standards.GRADIENT_RADIUS);
		return s;
	}

}
