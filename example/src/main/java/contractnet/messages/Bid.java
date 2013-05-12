package contractnet.messages;

import rinde.sim.core.model.communication.Message;

public class Bid extends Message{

	public final double value;
	
	public Bid(double value){
		this.value = value;
	}
}
