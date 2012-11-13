package rinde.sim.core.model.pdp2;

import rinde.sim.core.model.Model;
import rinde.sim.core.model.SimulatorModelAPI;
import rinde.sim.core.model.pdp.twpolicy.TimeWindowPolicy;
import rinde.sim.core.model.pdp2.objects.Parcel2;
import rinde.sim.util.TimeWindow;

public class PdpModel implements Model<PdpUser>, PdpAPI{

    private final TimeWindowPolicy twp;
    private SimulatorModelAPI simulatorAPI;
    
    public PdpModel(TimeWindowPolicy twp) {
        this.twp = twp;
    }
    

    // ----- TIME WINDOW POLICY ----- //
    
    @Override
    public boolean canPickup(Parcel2 parcel){
        return canPickup(parcel.pickupTimeWindow, simulatorAPI.getCurrentTime(), parcel.pickupDuration);
    }
    
    @Override
    public boolean canPickup(TimeWindow tw, long time, long duration){
        return twp.canPickup(tw, time, duration);
    }
    
    @Override
    public boolean canDeliver(Parcel2 parcel){
        return canPickup(parcel.deliveryTimeWindow, simulatorAPI.getCurrentTime(), parcel.deliveryDuration);
    }

    @Override
    public boolean canDeliver(TimeWindow tw, long time, long duration){
        return twp.canDeliver(tw, time, duration);
    }
    
    
    // ------ MODEL ------ //

    @Override
    public void setSimulatorAPI(SimulatorModelAPI api) {
        this.simulatorAPI = api;
    }

    @Override
    public void register(PdpUser element) {
        element.setPdpAPI(this);
    }

    @Override
    public void unregister(PdpUser element) {
        
    }

    @Override
    public Class<PdpUser> getSupportedType() {
        return PdpUser.class;
    }
}
