package rinde.sim.core.model.interaction.supported;

import rinde.sim.core.model.Unit;
import rinde.sim.core.model.interaction.apis.InteractiveAPI;
import rinde.sim.core.model.interaction.users.InteractiveUser;

public interface InteractiveUnit extends Unit{

    public InteractiveAPI getInteractiveAPI();
    
    public void setInteractiveAPI(InteractiveAPI api);
    
    @Override
    public InteractiveUser getElement();
}
