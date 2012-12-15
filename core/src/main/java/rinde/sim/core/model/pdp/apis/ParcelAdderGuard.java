package rinde.sim.core.model.pdp.apis;

import rinde.sim.core.model.Data;
import rinde.sim.core.model.InitGuard;
import rinde.sim.core.model.interaction.apis.InteractionAPI;
import rinde.sim.core.model.interaction.users.InteractionUser;
import rinde.sim.core.model.pdp.PdpModel;
import rinde.sim.core.model.pdp.receivers.SimpleDeliveryReceiver;
import rinde.sim.core.model.pdp.receivers.SimplePickupReceiver;
import rinde.sim.core.model.pdp.users.Parcel;

public class ParcelAdderGuard implements InteractionUser<Data>, InitGuard{

    private InteractionAPI api;
    private PdpModel pdpModel;
    private Parcel parcel;
    
    public ParcelAdderGuard(Parcel parcel, PdpModel pdpModel) {
        this.parcel = parcel;
        this.pdpModel = pdpModel;
    }
    
    @Override
    public void setInteractionAPi(InteractionAPI api) {
        this.api = api;
    }

    @Override
    public void init() {
        api.advertise(
                new SimplePickupReceiver<Parcel>(
                        parcel.location, parcel, pdpModel.getPolicy()));
        
        api.advertise(
                new SimpleDeliveryReceiver<Parcel>(
                        parcel.destination, parcel, pdpModel.getPolicy()));
    }

}
