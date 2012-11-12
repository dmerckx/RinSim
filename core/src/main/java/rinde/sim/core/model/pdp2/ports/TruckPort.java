package rinde.sim.core.model.pdp2.ports;

import rinde.sim.core.model.pdp2.PdpModel;
import rinde.sim.core.model.pdp2.users.Truck;
import rinde.sim.core.model.road.apis.RoadAPI;
import rinde.sim.core.model.road.ports.RoadPort;
import rinde.sim.core.simulation.TimeInterval;
import rinde.sim.core.simulation.types.Port;

public class TruckPort implements Port{

    private final RoadAPI roadAPI;
    private final PdpModel pdpModel;
    private final Truck truck;
    
    public TruckPort(Truck truck, PdpModel pdpModel, RoadAPI roadAPI) {
        
    }
    
    

}
