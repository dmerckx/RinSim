package rinde.sim.core.model.simulator.users;

import rinde.sim.core.model.User;
import rinde.sim.core.model.simulator.supported.SimulatorUnit;

public interface SimulatorUser extends User{

    @Override
    public SimulatorUnit buildUnit();
}
