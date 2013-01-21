package rinde.sim.core.simulation.policies;

import rinde.sim.core.model.InitUser;
import rinde.sim.core.model.User;
import rinde.sim.core.simulation.TickPolicy;
import rinde.sim.core.simulation.time.TimeLapseHandle;

public interface TimeUserPolicy extends TickPolicy{
    
    public void register(User<?> agent, TimeLapseHandle handle);
    
    public void unregister(User<?> agent);
    
    public void addInituser(InitUser user);
}
