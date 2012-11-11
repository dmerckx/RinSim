package rinde.sim.core.model.road.ports;

import rinde.sim.core.graph.Point;
import rinde.sim.core.model.road.RoadModel;
import rinde.sim.core.model.road.apis.FixedRoadAPI;
import rinde.sim.core.model.road.users.FixedRoadUser;
import rinde.sim.core.refs.ConstantRef;

public class FixedRoadPort extends RoadPort implements FixedRoadAPI{

    private FixedRoadUser user;

    private ConstantRef<Point> position;
    
    private boolean initialised = false;
    
    public FixedRoadPort(FixedRoadUser user, RoadModel model) {
        super(model);
        this.user = user;
    }
    
    @Override
    public boolean isInitialised(){
        return initialised;
    }

    @Override
    protected FixedRoadUser getUser() {
        return user;
    }
    
    
    // ------ ROAD API ------ //
    
    @Override
    public ConstantRef<Point> getPosition() {
        return position;
    }
    
    
    // ------ MOVING ROAD API ------ //

    @Override
    public void init(Point startLocation) {
        if(initialised) throw new IllegalStateException("Already initialised");
        
        this.position = new ConstantRef<Point>(startLocation);
        
        initialised = true;
    }
}
