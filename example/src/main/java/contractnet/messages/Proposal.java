package contractnet.messages;

import rinde.sim.core.graph.Point;
import rinde.sim.core.model.communication.Message;

public class Proposal extends Message{

	public final Point location;
	
	public Proposal(Point location){
		this.location = location;
	}
}
