package rinde.sim.core.model.pdp;

import rinde.sim.core.model.Data;
import rinde.sim.core.model.Model;
import rinde.sim.core.model.pdp.apis.ContainerAPI;
import rinde.sim.core.model.pdp.apis.ContainerGuard;
import rinde.sim.core.model.pdp.twpolicy.TimeWindowPolicy;
import rinde.sim.core.model.pdp.users.Container;
import rinde.sim.core.model.pdp.users.ContainerData;
import rinde.sim.core.model.pdp.users.Depot;
import rinde.sim.core.model.pdp.users.Parcel;
import rinde.sim.core.model.pdp.users.PdpUser;
import rinde.sim.core.model.pdp.users.Truck;
import rinde.sim.core.model.pdp.users.TruckData;
import rinde.sim.core.model.road.apis.RoadAPI;
import rinde.sim.core.simulation.SimulatorToModelAPI;
import rinde.sim.core.simulation.TimeInterval;

@SuppressWarnings("rawtypes")
public class PdpModel implements Model<Data, PdpUser<?>>, PdpAPI{

    private final TimeWindowPolicy twp;
    
    public PdpModel(TimeWindowPolicy twp) {
        this.twp = twp;
    }
    
    public TimeWindowPolicy getPolicy(){
        return twp;
    }
    
    // ------ MODEL ------ //

    @Override
    public void register(SimulatorToModelAPI sim, PdpUser<?> user, Data data) {
        
        if(user instanceof Container){
            Container cont = (Container) user;
            ContainerData contData = (ContainerData) data;
            
            ContainerGuard guard =
                    new ContainerGuard(cont, contData, this, sim.getApi(user, RoadAPI.class));
        }
        if(user instanceof Truck){
            Truck tr = (Truck) user;
            TruckData trData = (TruckData) data;
            
            TruckGuard guard = ...;
        }
        if(user instanceof Depot){
            registerDepot((Depot) user, (ContainerData) data);
        }
    }


    @Override
    public void unregister(PdpUser<?> user) {
        
    }

    @Override
    public Class<PdpUser<?>> getSupportedType() {
        return (Class) PdpUser.class;
    }

    @Override
    public void tick(TimeInterval time) {
        
    }
}
