package rinde.sim.core.model.pdp2.apis;

import java.util.List;

import rinde.sim.core.model.pdp.Parcel;
import rinde.sim.core.model.pdp.Vehicle;
import rinde.sim.core.model.pdp2.objects.Parcel2;
import rinde.sim.core.model.pdp2.users.Truck;
import rinde.sim.core.simulation.TimeLapse;

public interface TruckAPI extends ContainerAPI{
    
    public void init(double capacity);
    
    public TruckState getState();
    
    public List<Parcel2> getLoad();
    
    public Parcel2 tryPickup(TimeLapse lapse);
    
    public boolean tryDelivery(TimeLapse lapse);
    
    public List<Parcel2> scanLocation();

    
}
