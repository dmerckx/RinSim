package rinde.sim.core.model.simulator.supported;

import rinde.sim.core.model.Unit;
import rinde.sim.core.model.simulator.apis.SimulatorAPI;
import rinde.sim.core.model.simulator.users.SimulatorUser;

public interface SimulatorUnit extends Unit{

    public SimulatorAPI getSimulatorAPI();
    
    public void setSimulatorAPI(SimulatorAPI api);

    @Override
    public SimulatorUser getElement();

}
