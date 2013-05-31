package gradient2;

import rinde.sim.core.model.road.users.RoadUser;

public interface FieldEmitter2<D extends FieldData2> extends RoadUser<D>{
	
	public void setGradientModel(GradientModel2 model);
	
	/**
	 * Should always provide the same response during the same tick.
	 */
	public boolean isActive();
}
