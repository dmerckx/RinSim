package gradient;

import rinde.sim.core.model.road.users.RoadUser;

public interface FieldEmitter<D extends FieldData> extends RoadUser<D>{
	
	public void setGradientModel(GradientModel model);
	
	/**
	 * Should always provide the same response during the same tick.
	 */
	public boolean isActive();
}
