package gradient.model.users;

import gradient.model.apis.GradientAPI;
import gradient.model.apis.GradientState;
import rinde.sim.core.model.road.users.RoadUser;

public interface FieldEmitter<D extends FieldData> extends RoadUser<D>{
	
	public void setGradientAPI(GradientAPI api);
	
	public GradientState getGradientState();
}
