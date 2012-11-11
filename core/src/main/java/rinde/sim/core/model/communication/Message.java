package rinde.sim.core.model.communication;

import java.io.Serializable;

@SuppressWarnings("serial")
public abstract class Message implements Serializable {

	@Override
	public Message clone() throws CloneNotSupportedException{
		throw new CloneNotSupportedException();
	}
}
