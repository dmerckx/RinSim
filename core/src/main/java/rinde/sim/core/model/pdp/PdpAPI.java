package rinde.sim.core.model.pdp;

import rinde.sim.core.model.pdp.twpolicy.TimeWindowPolicy;

public interface PdpAPI{
    
    public TimeWindowPolicy getPolicy();

}
