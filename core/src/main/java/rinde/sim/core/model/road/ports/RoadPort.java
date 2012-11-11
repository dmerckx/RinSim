package rinde.sim.core.model.road.ports;

import java.io.Serializable;

import rinde.sim.core.model.road.RoadModel;
import rinde.sim.core.model.road.Visitor;
import rinde.sim.core.model.road.apis.RoadAPI;
import rinde.sim.core.model.road.users.RoadUser;

public abstract class RoadPort implements RoadAPI{

    protected RoadModel model;
    
    public RoadPort(RoadModel model) {
        this.model = model;
    }

    abstract protected boolean isInitialised();
    
    abstract protected RoadUser getUser();
    
    @Override
    public void unregister() {
        model.unregister(getUser());
    }

    @Override
    public <T extends RoadUser, R extends Serializable> R visitNode(
            Class<T> target, Visitor<T, R> visitor) {
        return null;
    }

}
