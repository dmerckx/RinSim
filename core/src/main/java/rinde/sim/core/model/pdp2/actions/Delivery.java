package rinde.sim.core.model.pdp2.actions;

import java.util.List;

import rinde.sim.core.model.pdp2.objects.Parcel2;
import rinde.sim.core.model.pdp2.users.ParcelDestination;
import rinde.sim.core.model.road.Visitor;

public class Delivery implements Visitor<ParcelDestination, Boolean>{

    private Parcel2 parcel;
    
    public Delivery(Parcel2 parcel){
        this.parcel = parcel;
    }
    
    @Override
    public Boolean visit(List<ParcelDestination> targets) {
        for(ParcelDestination target:targets){
            if(target.tryAccept(parcel)){
                return true;
            }
        }
        return false;
    }
}
