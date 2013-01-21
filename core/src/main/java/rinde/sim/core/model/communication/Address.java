package rinde.sim.core.model.communication;

import rinde.sim.core.model.communication.users.CommUser;


/**
 * Represents an address used to distinguish {@link CommUser}s.
 * 
 * @author dmerckx
 */
public final class Address {

	/**
	 * The id of this address.
	 */
	public final int id;
	
	/**
	 * Create a new address with a given id.
	 * @param id The id of this address.
	 */
	@SuppressWarnings("hiding")
    public Address(int id){
		this.id = id;
	}
	
	@Override
	public String toString() {
	    return "address{"+id+"}";
	}
}