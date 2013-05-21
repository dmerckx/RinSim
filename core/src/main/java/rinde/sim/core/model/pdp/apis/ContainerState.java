package rinde.sim.core.model.pdp.apis;

import rinde.sim.core.model.pdp.Parcel;
import rinde.sim.core.model.pdp.apis.ContainerAPI.ContState;

import com.google.common.collect.ImmutableList;

public abstract class ContainerState {

    ContainerState() {}
    
    public abstract ImmutableList<Parcel> getLoad();
    
    public abstract ContState getContState();
}
