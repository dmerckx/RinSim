package rinde.sim.core.model.pdp2.actions;

import java.util.List;

import rinde.sim.core.model.pdp2.objects.Parcel;
import rinde.sim.core.model.pdp2.users.ParcelDestination;
import rinde.sim.core.model.pdp2.users.ParcelLocation;
import rinde.sim.core.model.road.Visitor;
import rinde.sim.core.simulation.TimeLapse;

public class Delivery implements Visitor<ParcelDestination, Boolean>{

    private Parcel parcel;
    
    public Delivery(Parcel parcel){
        this.parcel = parcel;
    }
    
    @Override
    public Boolean visit(List<ParcelDestination> targets, TimeLapse time) {
        for(ParcelDestination target:targets){
            if(target.tryAccept(parcel)){
                return true;
            }
        }
        return false;
    }
}
