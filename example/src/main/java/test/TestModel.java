package test;

import java.util.List;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;

import rinde.sim.core.model.Data;
import rinde.sim.core.model.Model;
import rinde.sim.core.model.User;
import rinde.sim.core.simulation.TimeInterval;
import rinde.sim.core.simulation.UserInit;
import rinde.sim.core.simulation.policies.InteractionRules;
import rinde.sim.core.simulation.time.TimeLapseHandle;

import com.google.common.collect.Lists;

public class TestModel implements Model<Data, TestUser>{

	private InteractionRules rules;
	private RandomGenerator rng;
	
	void doInteraction(){
		rules.awaitAllPrevious();
	}
	
	long getSeed(){
		return rng.nextLong();
	}
	
	@SuppressWarnings("hiding")
	@Override
	public void init(long seed, InteractionRules rules, TimeInterval masterTime) {
		this.rules = rules;
		this.rng = new MersenneTwister(seed);
	}

	@Override
	public List<UserInit<?>> register(TestUser user, Data data, TimeLapseHandle handle) {
		user.setTestModel(this);
		
		return Lists.newArrayList();
	}

	@Override
	public void unregister(TestUser user) {}

	@Override
	public Class<TestUser> getSupportedType() {
		return TestUser.class;
	}

	@Override
	public void tick(TimeInterval time) {
		
	}

}
