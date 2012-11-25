package rinde.sim.core.dummies;

import rinde.sim.core.model.Unit;
import rinde.sim.core.simulation.TimeInterval;
import rinde.sim.core.simulation.TimeLapse;

public class DummyUnit implements Unit{
    private int count = 0;

    public int getTickCount() {
        return count;
    }

    private DummyUser user;
    
    public DummyUnit(DummyUser user) {
        this.user = user;
    }
    
    @Override
    public DummyUser getElement() {
        return user;
    }

    @Override
    public void init() {
        
    }

    @Override
    public void registerForTick(PreTick preTicker) {
        
    }

    @Override
    public void registerAfterTick(AfterTick afterTicker) {
        
    }

    @Override
    public void tick(TimeLapse lapse) {
        count++;
    }

    @Override
    public void afterTick(TimeInterval time) {
        
    }

}
