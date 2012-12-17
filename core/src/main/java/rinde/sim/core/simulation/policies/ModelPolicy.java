package rinde.sim.core.simulation.policies;

import rinde.sim.core.model.Model;
import rinde.sim.core.simulation.TimeInterval;

public class ModelPolicy extends Serial<Model<?,?>>{    
    

    public void register(Model<?,?> listener) {
        listeners.add(listener);
    }
    
    public void unregister(Model<?,?> listener) {
        listeners.remove(listener);
    }

    public ModelPolicy() {
        super(true);
    }

    @Override
    public void doTick(Model<?,?> obj, TimeInterval interval) {
        obj.tick(interval);
    }
}
