package rinde.sim.core.model.pdp;

import rinde.sim.core.model.pdp.supported.Parcel;
import rinde.sim.core.model.pdp.twpolicy.TimeWindowPolicy;
import rinde.sim.core.simulation.TimeLapse;

public interface PdpAPI extends TimeWindowPolicy{
    
    public boolean canPickup(TimeLapse time, Parcel parcel);
    
    public boolean canDeliver(TimeLapse time, Parcel parcel);

}
