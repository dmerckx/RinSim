package rinde.sim.core.model.pdp2;

import rinde.sim.core.model.pdp.twpolicy.TimeWindowPolicy;
import rinde.sim.core.model.pdp2.objects.Parcel2;

public interface PdpAPI extends TimeWindowPolicy{
    
    public boolean canPickup(Parcel2 parcel);
    
    public boolean canDeliver(Parcel2 parcel);

}
