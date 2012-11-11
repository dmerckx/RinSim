package rinde.sim.core.model.communication;

public class Delivery implements Comparable<Delivery>{
	
	public final Address address;
	public final Message message;
	
	public Delivery(Address address, Message message){
		this.address = address;
		this.message = message;
	}

	@Override
	public int compareTo(Delivery del) {
		return address.id < del.address.id ? -1 : 1;
	}
	
	@Override
	public Delivery clone() throws CloneNotSupportedException{
		return new Delivery(address, message.clone());
	}
}
