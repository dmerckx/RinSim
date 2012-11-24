package rinde.sim.core.model.pdp.supported;

import rinde.sim.core.model.interaction.supported.InteractiveUnit;
import rinde.sim.core.model.pdp.Parcel;
import rinde.sim.core.model.pdp.apis.ContainerAPI;
import rinde.sim.core.model.pdp.users.Container;
import rinde.sim.core.model.road.supported.RoadUnit;

public interface ContainerUnit<P extends Parcel> extends
    RoadUnit, InteractiveUnit, PdpUnit{

    public ContainerAPI<P> getContainerAPI();
    
    public void setContainerAPI(ContainerAPI<P> api);

    @Override
    public Container<P> getElement();
    
    @Override
    public ContainerData<P> getInitData();
    
    public interface ContainerData<P extends Parcel> extends RoadData{
        
        double getCapacity();
        
        Class<P> getParcelType();
    }
}
