package contractnet;

import rinde.sim.core.graph.Point;
import rinde.sim.core.model.communication.apis.CommunicationState;
import rinde.sim.core.model.communication.users.CommData;
import rinde.sim.core.model.communication.users.FullCommUser;
import rinde.sim.core.model.pdp.users.TruckData;
import contractnet.ContractTruck.CTData;

public class ContractTruck implements FullCommUser<CTData> {
	
	@Override
	public CommunicationState getCommunicationState() {
		return null;
	}
	
	public static class CTData extends TruckData.Std implements CommData{
		private final double reliablity;
		private final double radius;
		
		public CTData(double speed, Point pos, double cap, double rel, double radius) {
			super(speed, pos, cap);
			this.reliablity = rel;
			this.radius = radius;
		}

		@Override
		public Double getReliability() {
			return reliablity;
		}

		@Override
		public Double getInitialRadius() {
			return radius;
		}
	}
}
