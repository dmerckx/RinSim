package rinde.sim.core.model.pdp2;

import rinde.sim.core.model.pdp.twpolicy.TimeWindowPolicy;
import rinde.sim.core.model.pdp2.objects.Parcel;

public interface PdpAPI extends TimeWindowPolicy{
    
    public boolean canPickup(Parcel parcel);
    
    public boolean canDeliver(Parcel parcel);

}
