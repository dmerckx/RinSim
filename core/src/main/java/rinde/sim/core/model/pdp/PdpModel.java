package rinde.sim.core.model.pdp;

import rinde.sim.core.model.Model;
import rinde.sim.core.model.SimulatorModelAPI;
import rinde.sim.core.model.pdp.supported.Parcel;
import rinde.sim.core.model.pdp.supported.PdpType;
import rinde.sim.core.model.pdp.twpolicy.TimeWindowPolicy;
import rinde.sim.core.simulation.TimeLapse;
import rinde.sim.util.TimeWindow;

public class PdpModel implements Model<PdpType>, PdpAPI{

    private final TimeWindowPolicy twp;
    private SimulatorModelAPI simulatorAPI;
    
    public PdpModel(TimeWindowPolicy twp) {
        this.twp = twp;
    }
    
    // ----- PDP API ----- //
    
    @Override
    public boolean canPickup(TimeLapse lapse, Parcel parcel){
        return canPickup(parcel.pickupTimeWindow, lapse.getTime(), parcel.pickupDuration);
    }
    
    @Override
    public boolean canPickup(TimeWindow tw, long time, long duration){
        return twp.canPickup(tw, time, duration);
    }
    
    @Override
    public boolean canDeliver(TimeLapse lapse, Parcel parcel){
        return canPickup(parcel.deliveryTimeWindow, lapse.getTime(), parcel.deliveryDuration);
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
    public void register(PdpType element) {
        //TODO
    }

    @Override
    public void unregister(PdpType element) {
        //TODO
    }

    @Override
    public Class<PdpType> getSupportedType() {
        return PdpType.class;
    }
}
