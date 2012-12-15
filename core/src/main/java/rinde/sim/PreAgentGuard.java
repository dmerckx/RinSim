package rinde.sim;

import rinde.sim.core.model.Agent;
import rinde.sim.core.simulation.TimeLapse;

public interface PreAgentGuard extends Agent{

    public Agent getAgent();
    
    public void tick(TimeLapse time);
}
