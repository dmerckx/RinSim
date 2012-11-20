package rinde.sim.core.model.road.guards;

import rinde.sim.core.graph.Point;
import rinde.sim.core.model.road.RoadModel;
import rinde.sim.core.model.road.apis.FixedRoadAPI;
import rinde.sim.core.model.road.users.FixedRoadUser;
import rinde.sim.core.refs.ConstantRef;

public class FixedRoadGuard extends RoadGuard implements FixedRoadAPI{

    private FixedRoadUser user;

    private ConstantRef<Point> position;
    
    private boolean initialised = false;
    
    public FixedRoadGuard(FixedRoadUser user, RoadModel model) {
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

    @Override
    public Point getLocation() {
        return position.getValue();
    }
    
    
    // ------ ROAD API ------ //
    
    @Override
    public ConstantRef<Point> getPosition() {
        return position;
    }
    
    
    // ------ FIXED ROAD API ------ //

    @Override
    public void init(Point startLocation) {
        if(initialised) throw new IllegalStateException("Already initialised");
        
        this.position = new ConstantRef<Point>(startLocation);
        
        initialised = true;
    }
}
