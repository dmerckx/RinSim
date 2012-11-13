package rinde.sim.core.model;

import rinde.sim.core.model.simulator.SimulatorAPI;
import rinde.sim.core.simulation.types.AgentPort;

public interface SimulatorModelAPI extends SimulatorAPI {

    public void registerPort(AgentPort p);
    
    public void unregisterPort(AgentPort p);
    
    public long getCurrentTime();
}
