package rinde.sim.core.model.pdp.apis;

import rinde.sim.core.graph.Point;
import rinde.sim.core.model.pdp.PdpModel;
import rinde.sim.core.model.pdp.apis.PickupAPI.PickupState;
import rinde.sim.core.model.pdp.users.PickupPoint;
import rinde.sim.core.model.pdp.users.Truck;
import rinde.sim.core.model.pdp.users.TruckData;
import rinde.sim.core.model.road.apis.RoadAPI;
import rinde.sim.util.positions.Query;

//TODO
public class TruckGuard implements TruckAPI{

    private PdpModel pdpModel;
    private RoadAPI roadAPI;
    
    public TruckGuard(Truck<?> user, TruckData data, PdpModel model) {
        this.pdpModel = model;
    }

    @Override
    public void init(RoadAPI roadAPI, ContainerAPI containerAPI) {
        assert roadAPI == null: "RoadAPI can only be set ones";
        this.roadAPI = roadAPI;
    }
    
    @Override
    public synchronized Point findClosestAvailableParcel() {
        assert roadAPI != null: "Init has to be called first";
        
        ClosestAvailableParcelQuery q = new ClosestAvailableParcelQuery(roadAPI.getCurrentLocation());
        
        roadAPI.queryAround(roadAPI.getCurrentLocation(), pdpModel.range, q);
        
        return q.getCLosest();
        /*PickupPoint<?> p = pdpModel.queryClosestPickup(roadAPI.getCurrentLocation(), new Filter<PickupPoint<?>>() {
            @Override
            public boolean matches(PickupPoint<?> p) {
                return p.getPickupPointState().getPickupState() != PickupState.AVAILABLE;
            }
        });
        
        return p == null ? null : p.getPickupPointState().getParcel().location;*/
    }
}

class ClosestAvailableParcelQuery implements Query<PickupPoint<?>>{
    private final Point from;
    
    private Point closest;
    private double dist;
    
    public ClosestAvailableParcelQuery(Point from) {
        this.from = from;
        
        this.closest = null;
        this.dist = Double.POSITIVE_INFINITY;
    }
    
    public Point getCLosest(){
        return closest;
    }
    
    @Override
    public void process(PickupPoint<?> pp) {
        if(pp.getPickupPointState().getPickupState() != PickupState.AVAILABLE) return;
        
        double distPp = Point.distance(from, pp.getRoadState().getLocation());
        if(distPp < dist){
            closest = pp.getRoadState().getLocation();
            dist = distPp;
        }
    }

    @Override
    public Class<PickupPoint<?>> getType() {
        return (Class) PickupPoint.class;
    }
    
}
