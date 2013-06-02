package rinde.sim.core.model.gradient.queries;

import rinde.sim.core.graph.Point;
import rinde.sim.core.model.gradient.users.FieldEmitter;
import rinde.sim.core.model.pdp.users.PickupPoint;
import rinde.sim.core.model.pdp.users.Truck;
import rinde.sim.core.model.road.users.RoadUser;
import rinde.sim.util.positions.Query;

public class GetField implements Query{
    private final FieldEmitter<?> exclude;
    private final Point pos;

    private double field;

    public GetField(Point pos, FieldEmitter<?> exclude) {
        this.exclude = exclude;
        this.pos = pos;

        this.field = 0.0;
    }

    public double getField(){
        return field;
    }

    @Override
    public void process(RoadUser<?> obj) {
        if(!(obj instanceof Truck || obj instanceof PickupPoint))
            return;
        
        if(obj == exclude) return;
        //if(!fe.getGradientState().getIsActive()) return;
        Point feLoc = obj.getRoadState().getLocation();
        
        if(obj instanceof Truck){
            field -= 1.0d / Point.distance(pos, feLoc);
        }
        if(obj instanceof PickupPoint){
            field += 1.0d / Point.distance(pos, feLoc);
        }
    }

    @Override
    public Class<?> getType() {
        return FieldEmitter.class;
    }
}