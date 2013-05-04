package benchmark;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;

import rinde.sim.core.model.Agent;
import rinde.sim.core.model.Data;
import rinde.sim.core.model.User;
import rinde.sim.core.simulation.TimeLapse;

public class TestUser implements User<Data>, Agent{
	
	public static double max;
	
	private TestModel model;
	private RandomGenerator rng;
	private final double load;
	private final double interact;
	
	private long seed;
	
	public TestUser() {
		this(1.0d, 0.0d);
	}
	public TestUser(double load, double interact) {
		this.load = load;
		this.interact = interact;
		this.seed = 122;
	}
	
	void setTestModel(TestModel model){
		this.model = model;
		this.rng = new MersenneTwister(model.getSeed());
	}
	
	public static int NR = 0;
	public static synchronized void done(){
		NR++;
	}
	
	@Override
	public void tick(TimeLapse time) {
		if(rng.nextDouble() < interact){
			model.doInteraction();
		}
	
		double m = 0;
		for (int i = 1; i <= 10 * load; i++) {
		     double generated = nextDouble();
		     m = m > generated ? m : generated;
		}
		
		max = m;
		
		//done();
	}
	
	public double nextDouble(){
		return (((long) next(26) << 27) + next(27)) / (double) (1L << 53);
	}
	
	protected int next(int bits){
		seed = (seed * 0x5DEECE66DL + 0xBL) & ((1L << 48) - 1);
		return (int) (seed >>> (48 - bits));
	}
}
