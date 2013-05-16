package gradient.model;

import gradient.model.apis.GradientGuard;
import gradient.model.users.FieldData;
import gradient.model.users.FieldEmitter;

import java.util.List;

import rinde.sim.core.model.Model;
import rinde.sim.core.model.User;
import rinde.sim.core.model.road.RoadModel;
import rinde.sim.core.simulation.TimeInterval;
import rinde.sim.core.simulation.UserInit;
import rinde.sim.core.simulation.policies.InteractionRules;
import rinde.sim.core.simulation.time.TimeLapseHandle;
import rinde.sim.util.Rectangle;

import com.google.common.collect.Lists;

public class GradientModel implements Model<FieldData, FieldEmitter<?>>{

	public final double range;
	public final Rectangle bounds;
	
	public GradientModel(RoadModel rm, double range){
		this.range = range;
		this.bounds = rm.getViewRect();
	}

	// ----- MODEL ----- //

	@Override
	public List<UserInit<?>> register(FieldEmitter<?> user, FieldData data, TimeLapseHandle handle) {
		GradientGuard guard = new GradientGuard(user, data, this, handle);
		user.setGradientAPI(guard);
		
		return Lists.newArrayList();
	}

	@Override
	public void unregister(FieldEmitter<?> user) {
		
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
}