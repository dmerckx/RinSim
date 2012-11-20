package rinde.sim.core.model;

import rinde.sim.core.model.simulator.SimulatorAPI;

public interface SimulatorModelAPI extends SimulatorAPI {

    public void registerGuard(Guard p);
    
    public void unregisterGuard(Guard p);
    
    public long getCurrentTime();
}
