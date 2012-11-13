package rinde.sim.core.model.pdp2.users;

import java.util.ArrayList;
import java.util.List;

import rinde.sim.core.model.pdp2.objects.Parcel2;
import rinde.sim.core.model.road.apis.FixedRoadAPI;
import rinde.sim.core.model.road.users.FixedRoadUser;

public class Depot implements FixedRoadUser{
    
    private FixedRoadAPI roadAPI;
    private List<Parcel2> parcels = new ArrayList<Parcel2>();

    @Override
    public void initRoadUser(ContainerAPI api) {
        this.roadAPI = api;
    }
    
}
