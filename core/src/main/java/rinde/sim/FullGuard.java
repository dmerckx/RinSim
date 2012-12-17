package rinde.sim;

import rinde.sim.core.model.Agent;
import rinde.sim.core.simulation.TimeInterval;
import rinde.sim.core.simulation.TimeLapse;

public interface FullGuard extends Agent{

    void tick(TimeLapse time);
    
    void afterTick(TimeInterval time);
}
