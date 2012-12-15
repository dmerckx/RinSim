package rinde.sim.core.model.simulator.users;

import rinde.sim.core.model.Data;
import rinde.sim.core.model.User;
import rinde.sim.core.model.simulator.apis.SimulatorAPI;

public interface SimulatorUser<D extends Data> extends User<D>{

    void setSimulatorAPI(SimulatorAPI api);
}
