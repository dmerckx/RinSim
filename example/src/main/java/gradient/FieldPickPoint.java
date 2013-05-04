package gradient;

import gradient.FieldPickPoint.FPData;
import rinde.sim.core.model.pdp.Parcel;
import rinde.sim.core.model.pdp.users.PickupPoint;
import rinde.sim.core.model.pdp.users.PickupPointData;

public class FieldPickPoint extends PickupPoint<FPData> implements FieldEmitter<FPData>{
	@Override
	public void setGradientModel(GradientModel model) {
		//This user will not actively use the GradientModel
	}

	@Override
	public boolean isActive() {
		return true; //!pickupAPI.isPickedUp();
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

}

