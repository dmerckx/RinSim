package rinde.sim.core.model.pdp;

import rinde.sim.core.model.Model;
import rinde.sim.core.model.pdp.guards.ContainerGuard;
import rinde.sim.core.model.pdp.supported.ContainerUnit;
import rinde.sim.core.model.pdp.supported.PdpUnit;
import rinde.sim.core.model.pdp.twpolicy.TimeWindowPolicy;
import rinde.sim.core.simulation.TimeInterval;

@SuppressWarnings("rawtypes")
public class PdpModel implements Model<PdpUnit>, PdpAPI{

    private final TimeWindowPolicy twp;
    
    public PdpModel(TimeWindowPolicy twp) {
        this.twp = twp;
    }
    
    public TimeWindowPolicy getPolicy(){
        return twp;
    }
    
    // ------ MODEL ------ //

    @Override
    public void register(PdpUnit unit) {
        unit.setPdpAPI(this);
        
        if(unit instanceof ContainerUnit){
            registerContainerUnit((ContainerUnit<?>) unit);
        }
    }
    
    protected <P extends Parcel> void registerContainerUnit(ContainerUnit<P> unit){
        ContainerGuard<P> guard = new ContainerGuard<P>(unit, this);
        unit.setContainerAPI(guard);
    }

    @Override
    public void unregister(PdpUnit element) {
        
    }

    @Override
    public Class<PdpUnit> getSupportedType() {
        return PdpUnit.class;
    }

    @Override
    public void tick(TimeInterval time) {
        
    }
}
