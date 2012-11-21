package rinde.sim.core.model.pdp.users;

import rinde.sim.core.model.pdp.Parcel;
import rinde.sim.core.model.pdp.apis.TruckAPI;
import rinde.sim.core.model.pdp.supported.TruckUnit;
import rinde.sim.core.model.road.users.MovingRoadUser;
import rinde.sim.core.model.road.users.MovingRoadUser.MovingRoadData;

public abstract class Truck<P extends Parcel> implements Container<P>, MovingRoadUser{
   
    private TruckAPI<P> truckAPI;
    
    public void setTruckAPI(TruckAPI api){
        this.truckAPI = api;
    }
    
    @Override
    public abstract TruckData<P> initData();
  
    @Override
    public TruckUnit<P> buildUnit(){
        return new TruckUnit<P>(this);
    }
    
    public interface TruckData<P extends Parcel> extends ContainerData<P>, MovingRoadData{
        
    }
}
