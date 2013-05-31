package gradient2;

import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import rinde.sim.core.graph.Point;
import rinde.sim.core.model.Model;
import rinde.sim.core.model.road.RoadModel;
import rinde.sim.core.simulation.TimeInterval;
import rinde.sim.core.simulation.UserInit;
import rinde.sim.core.simulation.policies.InteractionRules;
import rinde.sim.core.simulation.time.TimeLapseHandle;
import rinde.sim.util.Rectangle;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class GradientModel2 implements Model<FieldData2, FieldEmitter2<?>>{

	private HashMap<FieldEmitter2<?>, Double> emitters = Maps.newLinkedHashMap();
	private Rectangle bounds;
	
	public GradientModel2(RoadModel rm){
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
	
	
	public Point getTargetFor(FieldEmitter2<?> fe, double dist){
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
	
	public double getField(Point in, FieldEmitter2<?> fe){
		double field = 0.0f;
		
		for(Entry<FieldEmitter2<?>, Double> e:emitters.entrySet()){
			FieldEmitter2<?> fe2 = e.getKey();
			Point fe2Loc = fe2.getRoadState().getLocation();
			
			if(fe2 == fe) continue;
			if(!fe2.isActive()) continue;
			
			field += e.getValue() / Point.distance(in, fe2Loc);
		}
		return field;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public Class<FieldEmitter2<?>> getSupportedType() {
		return (Class) FieldEmitter2.class;
	}

	@Override
	public void init(long seed, InteractionRules rules, TimeInterval masterTime) {
		
	}

	@Override
	public void tick(TimeInterval time) {
		
	}

	@Override
	public List<UserInit<?>> register(FieldEmitter2<?> user, FieldData2 data, TimeLapseHandle handle) {
		emitters.put(user, data.getStrenght());
		user.setGradientModel(this);
		
		return Lists.newArrayList();
	}

	@Override
	public void unregister(FieldEmitter2<?> user) {
		emitters.remove(user);
	}
}
