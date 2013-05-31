package gradient2;

import gradient2.FieldPickPoint2.FPData2;
import rinde.sim.core.model.pdp.Parcel;
import rinde.sim.core.model.pdp.users.PickupPoint;
import rinde.sim.core.model.pdp.users.PickupPointData;


public class FieldPickPoint2 extends PickupPoint<FPData2> implements FieldEmitter2<FPData2>{
	@Override
	public void setGradientModel(GradientModel2 model) {
		//This user will not actively use the GradientModel
	}

	@Override
	public boolean isActive() {
		return true; //!pickupAPI.isPickedUp();
	}
	
	public static class FPData2 extends PickupPointData.Std implements FieldData2{
		private final double strength;
		
		public FPData2(Parcel parcel, double strenght) {
			super(parcel);
			this.strength = strenght;
		}

		@Override
		public double getStrenght() {
			return strength;
		}
	}

}

