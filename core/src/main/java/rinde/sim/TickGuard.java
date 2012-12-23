package rinde.sim;

import rinde.sim.core.model.Agent;
import rinde.sim.core.simulation.TimeInterval;
import rinde.sim.core.simulation.TimeLapse;

public interface TickGuard extends Agent{

    void tick(TimeLapse time);
}
