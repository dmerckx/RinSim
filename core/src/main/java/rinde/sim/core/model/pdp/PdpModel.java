package rinde.sim.core.model.pdp;

import rinde.sim.core.model.Model;
import rinde.sim.core.model.SimulatorModelAPI;
import rinde.sim.core.model.pdp.guards.ContainerGuard;
import rinde.sim.core.model.pdp.supported.ContainerUnit;
import rinde.sim.core.model.pdp.supported.PdpType;
import rinde.sim.core.model.pdp.supported.SelfBuildParcel;
import rinde.sim.core.model.pdp.twpolicy.TimeWindowPolicy;

public class PdpModel implements Model<PdpType>, PdpAPI{

    private final TimeWindowPolicy twp;
    private SimulatorModelAPI simulatorAPI;
    
    public PdpModel(TimeWindowPolicy twp) {
        this.twp = twp;
    }
    
    public TimeWindowPolicy getPolicy(){
        return twp;
    }
    
    // ------ MODEL ------ //

    @Override
    public void setSimulatorAPI(SimulatorModelAPI api) {
        this.simulatorAPI = api;
    }

    @Override
    public void register(PdpType element) {
        if(element instanceof ContainerUnit){
            registerContainerUnit((ContainerUnit<?>) element);
        }
        else if(element instanceof SelfBuildParcel<?>){
            
        }
    }
    
    protected <P extends Parcel> void registerContainerUnit(ContainerUnit<P> unit){
        ContainerGuard<P> guard = new ContainerGuard<P>(unit.getElement(),
                unit.getRoadAPI(), unit.getInteractiveAPI(), this);
        unit.setContainerAPI(guard);
    }

    @Override
    public void unregister(PdpType element) {
        //TODO
    }

    @Override
    public Class<PdpType> getSupportedType() {
        return PdpType.class;
    }
}
