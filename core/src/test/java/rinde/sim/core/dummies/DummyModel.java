package rinde.sim.core.dummies;

import rinde.sim.core.model.Data;

public class DummyModel extends DummyAbstrModel<Data, DummyUser>{
    
	@Override
	public Class<DummyUser> getSupportedType() {
		return DummyUser.class;
	}
}