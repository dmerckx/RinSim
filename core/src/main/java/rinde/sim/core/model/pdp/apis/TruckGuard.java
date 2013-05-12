package rinde.sim.core.model.pdp.apis;

import java.util.List;

import rinde.sim.core.graph.Point;
import rinde.sim.core.model.SafeIterator;
import rinde.sim.core.model.pdp.Parcel;
import rinde.sim.core.model.pdp.PdpModel;
import rinde.sim.core.model.pdp.apis.PickupAPI.PickupState;
import rinde.sim.core.model.pdp.users.PickupPoint;
import rinde.sim.core.model.pdp.users.Truck;
import rinde.sim.core.model.pdp.users.TruckData;
import rinde.sim.core.model.road.apis.RoadAPI;
import rinde.sim.core.simulation.TimeLapse;
import rinde.sim.util.positions.Filter;

import com.google.common.collect.Lists;

//TODO
public class TruckGuard implements TruckAPI{

    private PdpModel pdpModel;
    private RoadAPI roadAPI;
    
    public TruckGuard(Truck<?> user, TruckData data, PdpModel model) {
        this.pdpModel = model;
    }

    @Override
    public void init(RoadAPI roadAPI, ContainerAPI containerAPI) {
        assert roadAPI == null: "RoadAPI can only be set ones";
        this.roadAPI = roadAPI;
    }
    
    @Override
    public Point findClosestAvailableParcel() {
        assert roadAPI != null: "Init has to be called first";
        
        PickupPoint<?> p = pdpModel.queryClosestPickup(roadAPI.getCurrentLocation(), new Filter<PickupPoint<?>>() {
            @Override
            public boolean matches(PickupPoint<?> p) {
                return p.getPickupPointState().getPickupState() != PickupState.AVAILABLE;
            }
        });
        
        return p == null ? null : p.getPickupPointState().getParcel().location;
    }
}
