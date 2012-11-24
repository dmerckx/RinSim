package rinde.sim.core.model.pdp.users;

import rinde.sim.core.model.User;
import rinde.sim.core.model.pdp.supported.PdpUnit;

public interface PdpUser extends User {

    @Override
    public PdpUnit buildUnit();
}
