package rinde.sim.core.model.pdp.apis;

import rinde.sim.core.graph.Point;
import rinde.sim.core.model.pdp.PdpModel;
import rinde.sim.core.model.pdp.apis.PickupAPI.PickupState;
import rinde.sim.core.model.pdp.users.PickupPoint;
import rinde.sim.core.model.pdp.users.Truck;
import rinde.sim.core.model.pdp.users.TruckData;
import rinde.sim.core.model.road.apis.RoadAPI;
import rinde.sim.core.model.road.users.RoadUser;
import rinde.sim.util.positions.Query;

//TODO
public class TruckGuard implements TruckAPI{

    private PdpModel pdpModel;
    private RoadAPI roadAPI;
    
    public TruckGuard(Truck<?> user, TruckData data, PdpModel model) {
        this.pdpModel = model;
    }

    @Override
    public void init(RoadAPI roadAPI) {
        assert this.roadAPI == null: "RoadAPI can only be set ones "; 
        this.roadAPI = roadAPI;
    }
    
    @Override
    public Point findClosestAvailableParcel() {
        assert roadAPI != null: "Init has to be called first";
        
        
        /*return pdpModel.getClosestParcel(roadAPI.getCurrentLocation());*/
        ClosestAvailableParcelQuery q = new ClosestAvailableParcelQuery(roadAPI.getCurrentLocation());
        
        roadAPI.queryAround(roadAPI.getCurrentLocation(), pdpModel.range, q);
        
        return q.getCLosest();
    }
}

class ClosestAvailableParcelQuery implements Query{
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
    public void process(RoadUser<?> obj) {
        if(!(obj instanceof PickupPoint))
            return;
        
        PickupPoint<?> pp = (PickupPoint<?>) obj;
        
        double distPp = Point.distance(from, pp.getRoadState().getLocation());
        if(distPp < dist &&
                pp.getPickupPointState().getPickupState() == PickupState.AVAILABLE){
            closest = pp.getRoadState().getLocation();
            dist = distPp;
        }
    }

    @Override
    public Class<?> getType() {
        return PickupPoint.class;
    }
}
