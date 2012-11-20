package rinde.sim.core.model.interaction.supported;

import rinde.sim.core.model.interaction.guards.InteractiveGuard;
import rinde.sim.core.model.interaction.users.InteractiveAgent;

public interface InteractiveHolder extends InteractiveType{

    public InteractiveGuard<?> getInteractiveGuard();
    
    public InteractiveAgent getElement();
}
