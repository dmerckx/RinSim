package gradient2;

import rinde.sim.core.graph.Point;
import rinde.sim.core.model.road.users.RoadData;

public interface FieldData2 extends RoadData {
	
	public double getStrenght();
	
	public class Std implements FieldData2{
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
