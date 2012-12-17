package rinde.sim.core.simulation.policies;

import rinde.sim.core.simulation.TickListener;
import rinde.sim.core.simulation.TickPolicy;

public interface TickListenerPolicy extends TickPolicy{
    

    public void register(TickListener listener);
    
    public void unregister(TickListener listener);

}
