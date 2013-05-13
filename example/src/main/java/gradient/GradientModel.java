package gradient;

import java.util.HashMap;
import java.util.List;

import rinde.sim.core.graph.Point;
import rinde.sim.core.model.Model;
import rinde.sim.core.model.User;
import rinde.sim.core.model.road.RoadModel;
import rinde.sim.core.simulation.TimeInterval;
import rinde.sim.core.simulation.UserInit;
import rinde.sim.core.simulation.policies.InteractionRules;
import rinde.sim.core.simulation.time.TimeLapseHandle;
import rinde.sim.util.Rectangle;
import rinde.sim.util.positions.Query;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class GradientModel implements Model<FieldData, FieldEmitter<?>>{

	private final RoadModel rm;
	private final double range;
	
	private HashMap<FieldEmitter<?>, Double> emitters = Maps.newLinkedHashMap();
	private Rectangle bounds;
	
	public GradientModel(RoadModel rm, double range){
		this.rm = rm;
		this.range = range;
		this.bounds = rm.getViewRect();
	}
	
	/**
	 * Possibilities
	 * (-1,1)	(0,1)	(1,1)
	 * (-1,0)			(1,0
	 * (-1,-1)	(0,-1)	(1,-1)
	 */
	private final int[] x = {	-1,	0, 	1, 	1, 	1, 	0, 	-1,	-1	};
	private final int[] y = {	1,	1,	1,	0,	-1,	-1,	-1,	0	};
	
	
	public Point getTargetFor(FieldEmitter<?> fe, double dist){
		double maxField = Double.NEGATIVE_INFINITY;
		Point maxFieldPoint = null;
		Point pos = fe.getRoadState().getLocation();
		
		for(int i = 0;i < x.length;i++){
			Point p = new Point(pos.x + dist * x[i], pos.y + dist * y[i]);
			
			if( p.x < bounds.xMin || p.x > bounds.xMax || p.y < bounds.yMin || p.y > bounds.yMax){
				continue;
			}
			
			double field = getField(p, fe);
			
			if(field >= maxField){
				maxField = field;
				maxFieldPoint = p;
			}
		}
		
		return maxFieldPoint;
	}
	
	public double getField(Point in, FieldEmitter<?> fe){
		GetField query = new GetField(in, fe, emitters);
		
		rm.queryAround(in, range, query);
		
		return query.getField();
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public Class<FieldEmitter<?>> getSupportedType() {
		return (Class) FieldEmitter.class;
	}

	@Override
	public void init(long seed, InteractionRules rules, TimeInterval masterTime) {
		
	}

	@Override
	public void tick(TimeInterval time) {
		
	}

	@Override
	public List<UserInit<?>> register(FieldEmitter<?> user, FieldData data, TimeLapseHandle handle) {
		emitters.put(user, data.getStrenght());
		user.setGradientModel(this);
		
		return Lists.newArrayList();
	}

	@Override
	public List<User<?>> unregister(FieldEmitter<?> user) {
		emitters.remove(user);
		
		return Lists.newArrayList();
	}
}

class GetField implements Query<FieldEmitter<?>>{
	
	private final HashMap<FieldEmitter<?>, Double> emitters;
	private final FieldEmitter<?> exclude;
	private final Point pos;
	
	private double field;
	
	public GetField(Point pos, FieldEmitter<?> exclude, HashMap<FieldEmitter<?>, Double> emitters) {
		this.emitters = emitters;
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
		if(!fe.isActive()) return;
		
		Point feLoc = fe.getRoadState().getLocation();
		
		field += emitters.get(fe) / Point.distance(pos, feLoc);
	}

	@Override
	public Class<FieldEmitter<?>> getType() {
		return (Class) FieldEmitter.class;
	}
	
}