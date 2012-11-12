package rinde.sim.core.model;

import rinde.sim.core.model.simulator.SimulatorAPI;
import rinde.sim.core.simulation.types.Port;

public interface SimulatorModelAPI extends SimulatorAPI {

    public void registerPort(Port p);
    
    public void unregisterPort(Port p);
    
    public long getCurrentTime();
}
