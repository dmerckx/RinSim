package rinde.sim.core.model.pdp.users;

import rinde.sim.core.model.pdp.apis.PickupAPI;
import rinde.sim.core.model.pdp.apis.PickupPointState;
import rinde.sim.core.model.road.apis.RoadAPI;
import rinde.sim.core.model.road.apis.RoadState;
import rinde.sim.core.model.road.users.FixedRoadUser;

public class PickupPoint<D extends PickupPointData> implements FixedRoadUser<D>, PdpUser<D>{
    
    protected PickupAPI pickupAPI;
    protected RoadAPI roadAPI;
    
    public final void setPickupAPI(PickupAPI api){
        this.pickupAPI = api;
    }
    
    public final PickupPointState getPickupPointState(){
        return pickupAPI.getState();
    }

    @Override
    public final void setRoadAPI(RoadAPI api) {
        this.roadAPI = api;
    }

    @Override
    public final RoadState getRoadState() {
        return roadAPI.getState();
    }
    
    public static class Std extends PickupPoint<PickupPointData>{}
}