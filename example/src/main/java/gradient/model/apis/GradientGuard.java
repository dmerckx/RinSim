package gradient.model.apis;

import gradient.model.GradientModel;
import gradient.model.users.FieldData;
import gradient.model.users.FieldEmitter;
import rinde.sim.core.graph.Point;
import rinde.sim.core.model.road.apis.RoadAPI;
import rinde.sim.core.simulation.time.TimeLapseHandle;
import rinde.sim.util.Rectangle;
import rinde.sim.util.concurrency.ValueCache;
import rinde.sim.util.concurrency.VariableValueCache;
import rinde.sim.util.positions.Query;

@SuppressWarnings("hiding")
public class GradientGuard extends GradientState implements GradientAPI {

    private final double strength;
    private final ValueCache<Boolean> active;
    private final FieldEmitter<?> user;

    private final GradientModel model;
    
    private RoadAPI roadAPI;
    
    public GradientGuard(FieldEmitter<?> user, FieldData data, GradientModel model, TimeLapseHandle handle) {
    	this.strength = data.getStrenght();
    	this.active = new VariableValueCache<Boolean>(true, handle);
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
		GetTarget q = new GetTarget(roadAPI.getCurrentLocation(), distance, user, model.bounds);
		roadAPI.queryAround(roadAPI.getCurrentLocation(), model.range, q);
		
		return q.getTarget();
		
		/*double maxField = Double.NEGATIVE_INFINITY;
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
		
		return maxFieldPoint;*/
	}

	/*@Override
	public double getField(Point pos) {
		GetField q = new GetField(pos, user);
		roadAPI.queryAround(pos, model.range, q);
		
		return q.getField();
	}*/

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

class GetTarget implements Query<FieldEmitter<?>>{
	private final FieldEmitter<?> exclude;
	private final double dist;
	private final Point pos;
	private final Rectangle bounds;

	private static final Point[] DIRECTIONS = {
		new Point(-1,1),
		new Point(0,1),
		new Point(1,1),
		new Point(1,0),
		new Point(1,-1),
		new Point(0,-1),
		new Point(-1,-1),
		new Point(-1,0)};
	private final Point[] directions; 
	private final double[] field;
	
	private double closestDist = Double.POSITIVE_INFINITY;
	private Point closest;
	
	public GetTarget(Point pos, double dist, FieldEmitter<?> exclude, Rectangle bounds) {
		this.bounds = bounds;
		this.pos = pos;
		this.exclude = exclude;
		this.dist = dist;
		this.field = new double[DIRECTIONS.length];
		this.directions = new Point[DIRECTIONS.length];
		
		for(int i = 0; i < DIRECTIONS.length; i++){
			directions[i] = new Point(pos.x + DIRECTIONS[i].x * dist, pos.y + DIRECTIONS[i].y * dist);
		}
	}
	
	public Point getTarget(){
		if(closestDist < dist)
			return closest;
		
		int fMax = -1;
		double maxField = Double.NEGATIVE_INFINITY;
		for(int f = 0; f < field.length; f++){
			if(bounds.isOutsideBounds(directions[f])) continue;
			
			if(field[f] > maxField){
				fMax = f;
				maxField = field[f];
			}
		}
		return directions[fMax];
	}
	
	@Override
	public void process(FieldEmitter<?> fe) {
		if(fe == exclude) return;
		if(!fe.getGradientState().getIsActive()) return;
		
		Point feLoc = fe.getRoadState().getLocation();
		

		for(int f = 0; f < field.length; f++){
			field[f] += fe.getGradientState().getStrength() / Point.distance(directions[f], feLoc);
		}
		
		if(Point.distance(pos, feLoc) < closestDist){
			closestDist = Point.distance(pos, feLoc);
			closest = pos;
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Class<FieldEmitter<?>> getType() {
		return (Class) FieldEmitter.class;
	}
}