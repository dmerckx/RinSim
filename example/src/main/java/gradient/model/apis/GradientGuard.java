package gradient.model.apis;

import gradient.model.GradientModel;
import gradient.model.users.FieldData;
import gradient.model.users.FieldEmitter;
import rinde.sim.core.graph.Point;
import rinde.sim.core.model.road.apis.RoadAPI;
import rinde.sim.core.simulation.time.TimeLapseHandle;
import rinde.sim.util.StateCache;
import rinde.sim.util.positions.Query;

@SuppressWarnings("hiding")
public class GradientGuard extends GradientState implements GradientAPI {

    private final double strength;
    private final StateCache<Boolean> active;
    private final FieldEmitter<?> user;

    private final GradientModel model;
    
    private RoadAPI roadAPI;
    
    public GradientGuard(FieldEmitter<?> user, FieldData data, GradientModel model, TimeLapseHandle handle) {
    	this.strength = data.getStrenght();
    	this.active = new StateCache<Boolean>(true, handle);
    	this.user = user;
    	
    	this.model = model;
	}

	@Override
	public void init(RoadAPI roadAPI) {
        assert this.roadAPI == null: "RoadAPI can only be set ones";
        this.roadAPI = roadAPI;
    }
	
	@Override
	public void setIsActive(boolean active) {
		this.active.setValue(active);
	}

	@Override
	public boolean getCurrentIsActive() {
		return active.getActualValue();
	}

	private final int[] x = {	-1,	0, 	1, 	1, 	1, 	0, 	-1,	-1	};
	private final int[] y = {	1,	1,	1,	0,	-1,	-1,	-1,	0	};
	@Override
	public Point getTarget(double distance) {
		double maxField = Double.NEGATIVE_INFINITY;
		Point maxFieldPoint = null;
		Point pos = roadAPI.getCurrentLocation();
		
		for(int i = 0;i < x.length;i++){
			Point p = new Point(pos.x + distance * x[i], pos.y + distance * y[i]);
			
			if(model.bounds.isOutsideBounds(p)){
				continue;
			}
			
			double field = getField(p);
			
			if(field >= maxField){
				maxField = field;
				maxFieldPoint = p;
			}
		}
		
		return maxFieldPoint;
	}

	@Override
	public double getField(Point pos) {
		GetField q = new GetField(pos, user);
		roadAPI.queryAround(pos, model.range, q);
		
		return q.getField();
	}

	@Override
	public GradientState getState() {
		return this;
	}

	// ----- GRADIENT STATE ----- //
	
	@Override
	public boolean getIsActive() {
		return active.getFrozenValue();
	}

	@Override
	public double getStrength() {
		return strength;
	}
}

class GetField implements Query<FieldEmitter<?>>{
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
	public void process(FieldEmitter<?> fe) {
		if(fe == exclude) return;
		if(!fe.getGradientState().getIsActive()) return;
		
		Point feLoc = fe.getRoadState().getLocation();
		
		field += fe.getGradientState().getStrength() / Point.distance(pos, feLoc);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Class<FieldEmitter<?>> getType() {
		return (Class) FieldEmitter.class;
	}
}
