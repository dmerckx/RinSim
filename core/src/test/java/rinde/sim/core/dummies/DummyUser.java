package rinde.sim.core.dummies;

import rinde.sim.core.model.User;

public class DummyUser implements User{
    
    private final DummyUnit unit;
    
    
    public DummyUser() {
        this.unit = new DummyUnit(this);
    }
    
    public DummyUnit getUnit(){
        return unit;
    }

    @Override
    public DummyUnit buildUnit() {
        return unit;
    }
}
