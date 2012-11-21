package rinde.sim.core.model.pdp.users;

import rinde.sim.core.model.interaction.users.InteractiveUser;
import rinde.sim.core.model.pdp.Parcel;
import rinde.sim.core.model.pdp.supported.ParcelAdderUnit;

public class ParcelAdder<P extends Parcel> implements InteractiveUser, PdpUser{

    @Override
    public ParcelAdderUnit<P> buildUnit() {
        //TODO
        return new ParcelAdderUnit<P>(null);
    }
}
