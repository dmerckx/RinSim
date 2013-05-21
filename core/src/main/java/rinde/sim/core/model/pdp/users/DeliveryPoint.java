package rinde.sim.core.model.pdp.users;

import rinde.sim.core.model.pdp.apis.DeliveryAPI;
import rinde.sim.core.model.pdp.apis.DeliveryPointState;
import rinde.sim.core.model.road.apis.RoadAPI;
import rinde.sim.core.model.road.apis.RoadState;
import rinde.sim.core.model.road.users.FixedRoadUser;

public class DeliveryPoint<D extends DeliveryPointData> implements FixedRoadUser<D>, PdpUser<D>{
    private static int idCounter = 0;
    private int id;
    
    protected DeliveryAPI deliveryAPI;
    protected RoadAPI roadAPI;
    
    public DeliveryPoint() {
        this.id = idCounter++;
    }
    
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
    
    @Override
    public String toString() {
        return "dp" + id;
    }
    

    public static class Std extends DeliveryPoint<DeliveryPointData>{}
}
