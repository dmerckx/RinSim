package rinde.sim.core.model.pdp.users;

import rinde.sim.core.model.interaction.users.InteractiveUser;
import rinde.sim.core.model.pdp.Parcel;
import rinde.sim.core.model.pdp.supported.ParcelAdderUnit;

public class ParcelAdder<P extends Parcel> implements InteractiveUser, PdpUser{

    private final P parcel;
    
    public ParcelAdder(P parcel) {
        this.parcel = parcel;
    }
    
    @Override
    public ParcelAdderUnit<P> buildUnit() {
        return new ParcelAdderUnit<P>(parcel);
    }
}
