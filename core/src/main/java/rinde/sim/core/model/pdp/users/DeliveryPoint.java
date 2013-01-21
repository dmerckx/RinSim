package rinde.sim.core.model.pdp.users;

import rinde.sim.core.model.pdp.apis.DeliveryAPI;
import rinde.sim.core.model.pdp.apis.DeliveryPointState;
import rinde.sim.core.model.road.apis.RoadAPI;
import rinde.sim.core.model.road.apis.RoadState;
import rinde.sim.core.model.road.users.FixedRoadUser;

public class DeliveryPoint<D extends DeliveryPointData> implements FixedRoadUser<D>, PdpUser<D>{
    
    protected DeliveryAPI deliveryAPI;
    protected RoadAPI roadAPI;
    
    public final void setDeliveryAPI(DeliveryAPI api){
        this.deliveryAPI = api;
    }
    
    public final DeliveryPointState getDeliveryPointState(){
        return deliveryAPI.getState();
    }

    @Override
    public final void setRoadAPI(RoadAPI api) {
        this.roadAPI = api;
    }

    @Override
    public final RoadState getRoadState() {
        return roadAPI.getState();
    }
    

    public static class Std extends DeliveryPoint<DeliveryPointData>{}
}
