package rinde.sim.core.model.communication;

import java.util.HashMap;
import java.util.List;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;

import rinde.sim.core.graph.Point;
import rinde.sim.core.model.Data;
import rinde.sim.core.model.Model;
import rinde.sim.core.model.communication.apis.CommAPI;
import rinde.sim.core.model.communication.apis.CommGuard;
import rinde.sim.core.model.communication.apis.SimpleCommGuard;
import rinde.sim.core.model.communication.users.CommData;
import rinde.sim.core.model.communication.users.CommUser;
import rinde.sim.core.model.communication.users.FullCommUser;
import rinde.sim.core.model.communication.users.SimpleCommData;
import rinde.sim.core.model.communication.users.SimpleCommUser;
import rinde.sim.core.model.road.RoadModel;
import rinde.sim.core.simulation.Simulator;
import rinde.sim.core.simulation.TimeInterval;
import rinde.sim.core.simulation.UserInit;
import rinde.sim.core.simulation.policies.InteractionRules;
import rinde.sim.core.simulation.time.TimeLapseHandle;
import rinde.sim.util.positions.Query;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * A model allowing its users to broadcast messages, send private
 * messages and obtain received messages.
 * 
 * This model supports the following types:
 *  - {@link SimpleCommUser} : {@link SimpleCommData}
 *  - {@link FullCommUser} : {@link CommData}
 *  
 * @author dmerckx
 */
public class CommunicationModel implements Model<Data, CommUser<?>>{

	//private final HashMap<Address, CommGuard> fullComms = Maps.newHashMap();
	private final HashMap<Address, SimpleCommGuard> comms = Maps.newHashMap();
	
	private int nextId = 0;
	private RandomGenerator rnd;
	
	private final RoadModel roadModel;

    public CommunicationModel(){
        this(null);
    }
	
	/**
	 * Create a new communication model.
	 */
	public CommunicationModel(RoadModel rm){
	    this.roadModel = rm;
	}
	
	/**
	 * Generate a new address, with a unique id.
	 * @return A new unique address.
	 */
	public Address generateAddress(){
		return new Address(nextId++);
	}
	
	/**
	 * Send a message to certain address.
	 * @param destination The destination to send to.
	 * @param msg The message to be delivered.
	 */
	public void send(Address destination, Delivery msg){
	    SimpleCommGuard receiver = comms.get(destination);
	    receiver.receive(msg);
	}
	
	/**
	 * Broadcast a message.
	 * @param msg The message to be broadcasted.
	 */
	public void broadcast(Delivery msg){
	    CommGuard sender = (CommGuard) comms.get(msg.sender);
		Point senderLocation = sender.getLastLocation();
		
	    /*FindAddresses query = new FindAddresses();
	    
	    roadModel.queryAround(senderLocation, sender.getRadius(), query);
	    
	    for(Address a:query.addresses){
	        CommGuard g = fullComms.get(a);
            try {
                g.receive(msg.clone());
            } catch (CloneNotSupportedException exc) {
                exc.printStackTrace();
            }
	    }*/
		
		DoBroadcast query = new DoBroadcast(msg);
		roadModel.queryAround(senderLocation, sender.getRadius(), query);
	}
	
	
	// ----- MODEL ----- //

	/**
	 * Register a communication user without going through the {@link Simulator}.
	 * @param user The user to register.
	 * @param data The initialization data of this user.
	 */
	public void register(CommUser<?> user, Data data) {
	    register(user, data, null);
	}
	
	@Override
	public List<UserInit<?>> register(CommUser<?> user, Data data, TimeLapseHandle handle) {
        assert user!=null : "User can not be null.";
	    assert data!=null : "Data can not be null.";
	    
	    SimpleCommGuard guard = null;
	    
	    if(user instanceof FullCommUser<?>){
	        guard = new CommGuard((FullCommUser<?>) user, (CommData) data, this, rnd.nextLong(), handle);
	        ((FullCommUser<?>) user).setCommunicationAPI((CommAPI) guard); 
	    }
	    else if(user instanceof SimpleCommUser<?>){
	        guard = new SimpleCommGuard((SimpleCommUser<?>) user, (SimpleCommData) data, this, rnd.nextLong(), handle);
	        ((SimpleCommUser<?>) user).setCommunicationAPI(guard);
        }
	    else {
            throw new IllegalArgumentException("unknown type received..");
	    }
	    
        comms.put(guard.getAddress(), guard);
        
        return Lists.newArrayList();
	}

	@Override
	public void unregister(CommUser<?> user) {
        assert user!=null : "User can not be null.";
        
        Address a = user.getCommunicationState().getAddress();
        
        assert comms.containsKey(a);
        
        comms.remove(a);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
	public Class<CommUser<?>> getSupportedType() {
		return (Class) CommUser.class;
	}

    @Override
    public void tick(TimeInterval time) {
        
    }
    
    @Override
    public void init(long seed, InteractionRules rules, TimeInterval masterTime) {
        this.rnd = new MersenneTwister(seed);
    }
}

class FindAddresses implements Query<FullCommUser<?>>{
    public final List<Address> addresses;
     
    public FindAddresses() {
        this.addresses = Lists.newArrayList();
    }
    
    @Override
    public void process(FullCommUser<?> t) {
        addresses.add(t.getCommunicationState().getAddress());
    }

    @Override
    public Class<FullCommUser<?>> getType() {
        return (Class) FullCommUser.class;
    }
}

class DoBroadcast implements Query<FullCommUser<?>>{
    private final Delivery msg; 
    
    public DoBroadcast(Delivery msg) {
        this.msg = msg;
    }
    
    @Override
    public void process(FullCommUser<?> t) {
        //A bit hacky, but most efficient
        SimpleCommGuard g = (SimpleCommGuard) t.getCommunicationState();
        
        try {
            g.receive(msg.clone());
        } catch (CloneNotSupportedException exc) {
            exc.printStackTrace();
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public Class<FullCommUser<?>> getType() {
        return (Class) FullCommUser.class;
    }
}
