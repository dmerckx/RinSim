package rinde.sim.core.model.pdp2.users;

import java.util.ArrayList;
import java.util.List;

import rinde.sim.core.model.pdp2.objects.Parcel;
import rinde.sim.core.model.road.apis.FixedRoadAPI;
import rinde.sim.core.model.road.users.FixedRoadUser;

public class Depot implements FixedRoadUser{
    
    private FixedRoadAPI roadAPI;
    private List<Parcel> parcels = new ArrayList<Parcel>();

    @Override
    public void initRoadUser(FixedRoadAPI api) {
        this.roadAPI = api;
    }
    
}
