package rinde.sim.core.simulation.policies;

import rinde.sim.core.model.Model;
import rinde.sim.core.simulation.TimeInterval;

public class ModelPolicy extends Serial<Model<?>>{

    public ModelPolicy() {
        super(true);
    }

    @Override
    public void doTick(Model<?> obj, TimeInterval interval) {
        obj.tick(interval);
    }
}
