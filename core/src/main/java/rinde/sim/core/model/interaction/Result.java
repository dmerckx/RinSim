package rinde.sim.core.model.interaction;

import rinde.sim.core.model.communication.Message;

/**
 * Represents the result of an interaction, which can be send between agents.
 * 
 * Just like other messages it is serializable to enforce users
 * not to include actual references to agents in this message.
 * 
 * It should not contain any references to agents or references to objects
 * shared among agents.
 * 
 * @author dmerckx
 */
@SuppressWarnings("serial")
public class Result extends Message{

}
