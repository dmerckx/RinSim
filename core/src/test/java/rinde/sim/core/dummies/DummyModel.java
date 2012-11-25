package rinde.sim.core.dummies;

import java.util.HashSet;

import rinde.sim.core.model.Model;
import rinde.sim.core.simulation.TimeInterval;

public class DummyModel extends DummyAbstrModel<DummyUnit>{
    
	@Override
	public Class<DummyUnit> getSupportedType() {
		return DummyUnit.class;
	}
}