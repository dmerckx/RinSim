package rinde.sim.core.model.interaction.users;

import rinde.sim.core.model.User;
import rinde.sim.core.model.interaction.supported.InteractiveUnit;

public interface InteractiveUser extends User {

    public InteractiveUnit buildUnit();
}
