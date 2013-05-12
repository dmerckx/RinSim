package contractnet.messages;

import rinde.sim.core.graph.Point;
import rinde.sim.core.model.communication.Message;

public class Winner extends Message{

	public final Point location;
	
	public Winner(Point location){
		this.location = location;
	}
}
