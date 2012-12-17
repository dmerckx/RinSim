package rinde.sim.core.model.pdp.apis;

import java.util.List;

import rinde.sim.core.model.pdp.Parcel;
import rinde.sim.core.model.pdp.apis.ContainerAPI.ContState;

public abstract class ContainerState {

    ContainerState() {}
    
    public abstract List<Parcel> getLoad();
    
    public abstract ContState getContState();
}
