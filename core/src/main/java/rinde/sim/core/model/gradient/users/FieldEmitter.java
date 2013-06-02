package rinde.sim.core.model.gradient.users;

import rinde.sim.core.model.gradient.apis.GradientAPI;
import rinde.sim.core.model.gradient.apis.GradientState;
import rinde.sim.core.model.road.users.RoadUser;

public interface FieldEmitter<D extends FieldData> extends RoadUser<D>{
	
	public void setGradientAPI(GradientAPI api);
	
	public GradientState getGradientState();
}
