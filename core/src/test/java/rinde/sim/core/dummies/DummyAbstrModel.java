package rinde.sim.core.dummies;

import java.util.ArrayList;
import java.util.List;

import rinde.sim.core.model.Data;
import rinde.sim.core.model.Model;
import rinde.sim.core.model.User;
import rinde.sim.core.simulation.TimeInterval;
import rinde.sim.core.simulation.UserInit;

import com.google.common.collect.Lists;

public abstract class DummyAbstrModel<D extends Data, U extends User<? extends D>> implements Model<D, U> {
    private int count = 0;

    public int getTickCount() {
        return count;
    }

    public final List<U> objs;

    public DummyAbstrModel() {
        objs = new ArrayList<U>();
    }

    @Override
    public List<UserInit<?>> register(U unit, D data) {
        objs.add(unit);
        
        return Lists.newArrayList();
    }

    @Override
    public List<User<?>> unregister(U unit) {
        objs.remove(unit);

        return Lists.newArrayList();
    }
    
    @Override
    public void tick(TimeInterval time) {
        count++;
    }
}