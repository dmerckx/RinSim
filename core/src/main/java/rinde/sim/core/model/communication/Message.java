package rinde.sim.core.model.communication;

import java.io.Serializable;

/**
 * Represents a message, which can be send between agents.
 * A message is serializable to enforce users not to include
 * actual references to agents in this message.
 * 
 * A message may contain state (a smart message) but should
 * not contain any references to agents or references to objects
 * shared among agents.
 *  
 * Including references to shared objects could break determinism
 * of the simulation.
 * 
 * Note that this message is never serialized (for performance
 * reasons) but it should be designed as if it would be.
 * 
 * @author dmerckx
 */
@SuppressWarnings("serial")
public class Message implements Serializable {

	@Override
	public Message clone() throws CloneNotSupportedException{
		throw new CloneNotSupportedException();
	}
}
