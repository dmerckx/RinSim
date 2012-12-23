package rinde.sim.core.model.communication;

/**
 * A wrapper around a message, which contains the sender
 * of this message as well.
 * 
 * @author dmerckx
 */
public class Delivery implements Comparable<Delivery>{
	
    /**
     * The address from which this message originates.
     */
	public final Address sender;
	/**
	 * The actual message.
	 */
	public final Message message;
	
	/**
	 * Construct a new delivery wrapper by providing a sender
	 * and a message.
	 * @param sender The send of this message.
	 * @param message The actual message.
	 */
	@SuppressWarnings("hiding")
    public Delivery(Address sender, Message message){
		this.sender = sender;
		this.message = message;
	}

	@Override
	public int compareTo(Delivery del) {
		return sender.id < del.sender.id ? -1 : 1;
	}
	
	@Override
	public Delivery clone() throws CloneNotSupportedException{
		return new Delivery(sender, message.clone());
	}
}
