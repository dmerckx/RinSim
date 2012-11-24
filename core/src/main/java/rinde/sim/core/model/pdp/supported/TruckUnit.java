package rinde.sim.core.model.pdp.supported;

import rinde.sim.core.graph.Point;
import rinde.sim.core.model.UnitImpl;
import rinde.sim.core.model.interaction.apis.InteractiveAPI;
import rinde.sim.core.model.pdp.Parcel;
import rinde.sim.core.model.pdp.PdpAPI;
import rinde.sim.core.model.pdp.apis.ContainerAPI;
import rinde.sim.core.model.pdp.users.Truck;
import rinde.sim.core.model.road.apis.MovingRoadAPI;
import rinde.sim.core.model.road.apis.RoadAPI;
import rinde.sim.core.model.road.supported.MovingRoadUnit;

public class TruckUnit<P extends Parcel> extends UnitImpl implements ContainerUnit<P>, MovingRoadUnit{

    private MovingRoadAPI roadAPI;
    private InteractiveAPI interactiveAPI;
    private ContainerAPI<P> containerAPI;
    private PdpAPI pdpAPI;

    private Truck<P> truck;
    
    //Initial data
    public final Point initPos;
    public final double initCap;
    public final Class<P> parcelType;
    public final double initSpeed;
    
    public TruckUnit(Truck truck, Point pos, double cap, Class<P> type, double speed) {
        this.truck = truck;
        this.initPos = pos;
        this.initCap = cap;
        this.parcelType = type;
        this.initSpeed = speed;
    }
    
    @Override
    public void init() {
        
    }
    
    @Override
    public MovingRoadAPI getRoadAPI() {
        return roadAPI;
    }

    @Override
    public void setRoadAPI(RoadAPI api) {
        this.roadAPI = (MovingRoadAPI) api;
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
    public PdpAPI getPdpAPI() {
        return pdpAPI;
    }

    @Override
    public void setPdpAPI(PdpAPI api) {
        this.pdpAPI = api;
    }

    @Override
    public ContainerAPI<P> getContainerAPI() {
        return containerAPI;
    }

    @Override
    public void setContainerAPI(ContainerAPI<P> api) {
        this.containerAPI = api;
    }

    @Override
    public Truck<P> getElement() {
        return truck;
    }
    
    @Override
    public TruckData<P> getInitData() {
        return new TruckData<P>() {
            @Override
            public Double getInitialSpeed() {   return initSpeed;   }
            
            @Override
            public Point getStartPosition() {   return initPos;     }
            
            @Override
            public Class<P> getParcelType() {   return parcelType;  }
            
            @Override
            public double getCapacity() {       return initCap;     }
        };
    }
    
    public interface TruckData<P extends Parcel> extends ContainerData<P>, MovingRoadData{
        
    }
}
