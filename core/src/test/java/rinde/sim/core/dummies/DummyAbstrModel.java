package rinde.sim.core.dummies;

import java.util.ArrayList;
import java.util.List;

import rinde.sim.core.model.Model;
import rinde.sim.core.model.Unit;
import rinde.sim.core.simulation.TimeInterval;

public abstract class DummyAbstrModel<U extends Unit> implements Model<U> {
    private int count = 0;

    public int getTickCount() {
        return count;
    }

    private final List<U> objs;

    public DummyAbstrModel() {
        objs = new ArrayList<U>();
    }

    @Override
    public void register(U unit) {
        objs.add(unit);
    }

    @Override
    public void unregister(U unit) {
        objs.remove(unit);
    }
    
    @Override
    public void tick(TimeInterval time) {
        count++;
    }
}