package rinde.sim.examples.benchmark.simple;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;

import rinde.sim.core.model.Agent;
import rinde.sim.core.model.Data;
import rinde.sim.core.model.User;
import rinde.sim.core.simulation.TimeLapse;

public class TestUser implements User<Data>, Agent{
	
	public static double resultPi;
	
	private TestModel model;
	private NormalDistribution distrib;
	private RandomGenerator rng;
	private final double load;
	private final double interact;
	
	public TestUser() {
		this(1.0d, 0.0d);
	}
	public TestUser(double load, double interact) {
		this.load = load;
		this.interact = interact;
	}
	
	void setTestModel(TestModel model){
		this.model = model;
		this.distrib = new NormalDistribution(10d, 4d, NormalDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY);
		distrib.reseedRandomGenerator(model.getSeed());
		this.rng = new MersenneTwister(model.getSeed());
	}
	
	@Override
	public void tick(TimeLapse time) {
		
		if(rng.nextDouble() < interact) model.doInteraction();
		
		int iterations = (int) (distrib.sample() * 10 * load);
	
		double sum = 0;
		for (int i = 1; i <= iterations; i++) {
		     double numToAdd = 4.0 / ((i * 2) + 1);
		     sum = i % 2 == 0 ? sum + numToAdd : sum - numToAdd;
		}
		
		resultPi = sum;
	}
}
