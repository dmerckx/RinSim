package gradient;

import gradient.FieldPickPoint.FPData;
import gradient.model.apis.GradientAPI;
import gradient.model.apis.GradientState;
import gradient.model.users.FieldData;
import gradient.model.users.FieldEmitter;
import rinde.sim.core.model.pdp.Parcel;
import rinde.sim.core.model.pdp.users.PickupPoint;
import rinde.sim.core.model.pdp.users.PickupPointData;

public class FieldPickPoint extends PickupPoint<FPData> implements FieldEmitter<FPData>{
	private GradientAPI gradientAPI;
	
	@Override
	public void setGradientAPI(GradientAPI api) {
		api.init(roadAPI);
		this.gradientAPI = api;
	}
	
	public static class FPData extends PickupPointData.Std implements FieldData{
		private final double strength;
		
		public FPData(Parcel parcel, double strenght) {
			super(parcel);
			this.strength = strenght;
		}

		@Override
		public double getStrenght() {
			return strength;
		}
	}

	@Override
	public GradientState getGradientState() {
		return gradientAPI.getState();
	}

}

