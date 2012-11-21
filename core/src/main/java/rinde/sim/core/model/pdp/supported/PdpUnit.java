package rinde.sim.core.model.pdp.supported;

import rinde.sim.core.model.Unit;
import rinde.sim.core.model.pdp.PdpAPI;
import rinde.sim.core.model.pdp.users.PdpUser;

public interface PdpUnit extends Unit{

    public PdpAPI getPdpAPI();
    
    public void setPdpAPI(PdpAPI guard);

    @Override
    public PdpUser getElement();
}
