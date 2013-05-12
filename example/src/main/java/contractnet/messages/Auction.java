package contractnet.messages;

import rinde.sim.core.graph.Point;
import rinde.sim.core.model.communication.Message;

public class Auction extends Message{

	public final Point location;
	
	public Auction(Point location){
		this.location = location;
	}
	
	@Override
	public Message clone() throws CloneNotSupportedException {
		return new Auction(location);
	}
}
