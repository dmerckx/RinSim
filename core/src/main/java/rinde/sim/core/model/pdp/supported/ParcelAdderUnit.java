package rinde.sim.core.model.pdp.supported;

import rinde.sim.core.model.UnitImpl;
import rinde.sim.core.model.interaction.Receiver;
import rinde.sim.core.model.interaction.apis.InteractiveAPI;
import rinde.sim.core.model.interaction.supported.InteractiveUnit;
import rinde.sim.core.model.pdp.Parcel;
import rinde.sim.core.model.pdp.PdpAPI;
import rinde.sim.core.model.pdp.receivers.SimpleDeliveryReceiver;
import rinde.sim.core.model.pdp.receivers.SimplePickupReceiver;
import rinde.sim.core.model.pdp.users.ParcelAdder;
import rinde.sim.core.simulation.TimeLapse;

public class ParcelAdderUnit<P extends Parcel> extends UnitImpl implements InteractiveUnit, PdpUnit {

    private InteractiveAPI interactiveAPI;
    private PdpAPI pdpAPI;
    
    private ParcelAdder<P> element;
    private boolean setup = false;
    private final P parcel;
    
    public ParcelAdderUnit(P parcel) {
        this.parcel = parcel;
    }
    
    protected Receiver buildPickup(){
        return new SimplePickupReceiver<P>(parcel.location, parcel, pdpAPI.getPolicy());
    }
    
    protected Receiver buildDelivery(){
        return new SimpleDeliveryReceiver<P>(parcel.destination, parcel, pdpAPI.getPolicy());
    }

    @Override
    public void init() {
        
    }
    
    @Override
    public void tick(TimeLapse lapse) {
        if(!setup){
            interactiveAPI.advertise(buildPickup());
            interactiveAPI.advertise(buildDelivery());
            setup = true;
        }
        super.tick(lapse);
    }

    @Override
    public InteractiveAPI getInteractiveAPI() {
        return interactiveAPI;
    }

    @Override
    public void setInteractiveAPI(InteractiveAPI api) {
        this.interactiveAPI = api;
    }
    
    @Override
    public PdpAPI getPdpAPI(){
        return pdpAPI;
    }
    
    @Override
    public void setPdpAPI(PdpAPI api){
        this.pdpAPI = api;
    }

    @Override
    public ParcelAdder<P> getElement() {
        return element;
    }
}
