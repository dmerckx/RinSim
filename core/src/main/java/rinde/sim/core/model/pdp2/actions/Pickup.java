package rinde.sim.core.model.pdp2.actions;

import java.util.List;

import rinde.sim.core.model.pdp2.objects.Parcel;
import rinde.sim.core.model.pdp2.users.ParcelLocation;
import rinde.sim.core.model.road.Visitor;
import rinde.sim.core.simulation.TimeLapse;

public class Pickup implements Visitor<ParcelLocation, Parcel>{

    
    public Pickup() {}
     
    @Override
    public Parcel visit(List<ParcelLocation> targets, TimeLapse time) {
        if(targets.size() == 0)
            return null;
        
        return null;
    }
}
