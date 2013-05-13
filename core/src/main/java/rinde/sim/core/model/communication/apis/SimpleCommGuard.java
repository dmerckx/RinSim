package rinde.sim.core.model.communication.apis;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;

import rinde.sim.core.model.communication.Address;
import rinde.sim.core.model.communication.CommunicationModel;
import rinde.sim.core.model.communication.Delivery;
import rinde.sim.core.model.communication.Message;
import rinde.sim.core.model.communication.users.CommUser;
import rinde.sim.core.model.communication.users.SimpleCommData;
import rinde.sim.core.simulation.time.TimeLapseHandle;

/**
 * An implementation of the {@link CommAPI}.
 * 
 * This guard guarantees consistency for receiving messages:
 *  - new messages are received in a thread safe way
 *  - new messages are not shown to the user until the next turn
 *  - the state of this API will never change during a single tick
 * 
 * @author dmerckx
 */
public class SimpleCommGuard extends CommunicationState
        implements SimpleCommAPI{
	
	private final List<Delivery> mailbox;
	private final SortedSet<Delivery> tempMailbox;
	private boolean active = false;
	
	@SuppressWarnings("javadoc")
    protected Address address;

    @SuppressWarnings("javadoc")
	protected final CommunicationModel model;
    @SuppressWarnings("javadoc")
    protected final RandomGenerator rnd;
    private final int seed;
    private final TimeLapseHandle handle;
    
    @SuppressWarnings("javadoc")
    protected double reliability;
	
	/**
	 * Construct a new guard. 
	 * @param user The user to which this API belongs.
	 * @param data The initialization data for this API.
	 * @param model The communication model.
	 * @param seed The seed used for generating random number.
	 * @param handle The handle to the user's time lapse.
	 */
	@SuppressWarnings("hiding")
    public SimpleCommGuard(CommUser<?> user, SimpleCommData data, CommunicationModel model, long seed, TimeLapseHandle handle){
	    super();
		this.model = model;
		this.rnd = new MersenneTwister(seed);
		this.address = model.generateAddress();
		this.mailbox = new ArrayList<Delivery>();
		this.tempMailbox = new TreeSet<Delivery>();
        this.reliability = data.getReliability();
        this.handle = handle;
        this.seed = rnd.nextInt();
	}
	
	/**
	 * Returns whether or not this guard received at least one message
	 * during the last tick. 
	 * @return Whether a message was received.
	 */
	public final boolean isActive(){
	    return active;
	}
	
    /**
     * Receive a new message in a thread safe way.
     * @param delivery The delivery of a new message.
     */
    public final synchronized void receive(Delivery delivery){
        tempMailbox.add(delivery);
        active = true;
    }

    /**
     * Used by the {@link CommunicationModel} after a tick to process all received
     * messages and add them to the mailbox. 
     */
    public void process() {
        mailbox.addAll(tempMailbox);
        tempMailbox.clear();
        active = false;
    }
    
    
    // ----- COMMUNICATION API -----

	@Override
	public void send(Address destination, Message message) {
	    if(rnd.nextFloat() <= reliability)
	        model.send(destination, new Delivery(address, message));
	}
	@Override
	public Iterator<Delivery> getMessages() {
		return mailbox.iterator();
	}

    
    // ----- STATE ----- //
    
    @Override
    public Address getAddress() {
        return address;
    }

    @Override
    public CommunicationState getState() {
        return this;
    }
}
