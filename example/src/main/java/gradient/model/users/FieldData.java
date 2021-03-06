package gradient.model.users;

import rinde.sim.core.graph.Point;
import rinde.sim.core.model.road.users.RoadData;

public interface FieldData extends RoadData {
	
	public double getStrenght();
	
	public class Std implements FieldData{
		private final Point startPos;
		private final double strenght;
		
		public Std(Point startPos, double strenght) {
			this.startPos = startPos;
			this.strenght = strenght;
		}
		
		@Override
		public Point getStartPosition() {
			return startPos;
		}

		@Override
		public double getStrenght() {
			return strenght;
		}
		
	}
}
