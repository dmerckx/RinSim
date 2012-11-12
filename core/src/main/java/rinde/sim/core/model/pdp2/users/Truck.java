package rinde.sim.core.model.pdp2.users;

import java.util.ArrayList;
import java.util.List;

import rinde.sim.core.model.pdp2.actions.Pickup;
import rinde.sim.core.model.pdp2.objects.Parcel;
import rinde.sim.core.model.road.apis.MovingRoadAPI;
import rinde.sim.core.model.road.users.MovingRoadUser;

public class Truck implements MovingRoadUser{
   
    private MovingRoadAPI roadAPI;
    private List<Parcel> load = new ArrayList<Parcel>();
    
    public Truck() {
        
    }
    
    @Override
    public void initRoadUser(MovingRoadAPI api) {
        this.roadAPI = api;
    }
    
    public boolean tryPickup(){
        Parcel result = roadAPI.visitNode(new Pickup());
        
        if( result == null) return false;
        
        load.add(result);
        return true; 
    }
}
