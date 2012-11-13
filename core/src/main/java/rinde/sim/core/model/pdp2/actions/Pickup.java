package rinde.sim.core.model.pdp2.actions;

import java.util.List;

import rinde.sim.core.model.pdp2.objects.Parcel2;
import rinde.sim.core.model.pdp2.users.ParcelLocation;
import rinde.sim.core.model.road.Visitor;
import rinde.sim.core.simulation.TimeLapse;

public class Pickup implements Visitor<ParcelLocation, Parcel2>{

    
    public Pickup() {}
     
    @Override
    public Parcel2 visit(List<ParcelLocation> targets) {
        if(targets.size() == 0)
            return null;
        
        return null;
    }
}
