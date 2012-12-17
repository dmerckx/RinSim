package rinde.sim.core.simulation.policies;

import java.util.List;

import rinde.sim.core.model.TimeUser;
import rinde.sim.core.model.User;
import rinde.sim.core.simulation.TickPolicy;

public interface TimeUserPolicy extends TickPolicy{
    
    public void register(User<?> originalUser, List<TimeUser> users);
    
    public void unregister(User<?> originalUser);
}
