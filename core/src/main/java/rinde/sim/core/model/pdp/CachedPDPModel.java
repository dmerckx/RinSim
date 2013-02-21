package rinde.sim.core.model.pdp;

import rinde.sim.core.graph.Point;
import rinde.sim.core.model.pdp.twpolicy.TimeWindowPolicy;
import rinde.sim.core.model.pdp.users.PickupPoint;
import rinde.sim.core.model.road.RoadModel;
import rinde.sim.util.Rectangle;
import rinde.sim.util.positions.Filter;
import rinde.sim.util.positions.PositionCache;

public class CachedPDPModel extends PdpModel{

    private PositionCache<PickupPoint<?>> pickupsCache;
    
    public CachedPDPModel(TimeWindowPolicy twp, RoadModel rm) {
        this(twp, null, rm);
    }
    
    public CachedPDPModel(TimeWindowPolicy twp, PdpObserver observer, RoadModel rm) {
        super(twp, observer);
        
        Rectangle rect = rm.getViewRect();
        
        pickupsCache = new PositionCache<PickupPoint<?>>(rect.xMin, rect.xMax, rect.yMin, rect.yMax);
    }
    
    @Override
    protected void registerPickupPoint(PickupPoint<?> p) {
        pickupsCache.add(p.getRoadState().getLocation(), p);
    }
    
    @Override
    protected void unregisterPickupPoint(PickupPoint<?> p) {
        pickupsCache.remove(p.getRoadState().getLocation(), p);
    }
    
    @Override
    public PickupPoint<?> queryClosestPickup(Point pos, Filter<PickupPoint<?>> filter) {
        return pickupsCache.getClosestTo(pos, filter);
    }

}
