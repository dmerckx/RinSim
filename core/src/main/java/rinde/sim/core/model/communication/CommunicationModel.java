package rinde.sim.core.model.communication;

import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;

import rinde.sim.core.graph.Point;
import rinde.sim.core.model.Data;
import rinde.sim.core.model.Model;
import rinde.sim.core.model.User;
import rinde.sim.core.model.communication.apis.CommGuard;
import rinde.sim.core.model.communication.apis.SimpleCommGuard;
import rinde.sim.core.model.communication.users.CommData;
import rinde.sim.core.model.communication.users.CommUser;
import rinde.sim.core.model.communication.users.FullCommUser;
import rinde.sim.core.model.communication.users.SimpleCommData;
import rinde.sim.core.model.communication.users.SimpleCommUser;
import rinde.sim.core.simulation.Simulator;
import rinde.sim.core.simulation.TimeInterval;
import rinde.sim.core.simulation.UserInit;
import rinde.sim.core.simulation.policies.InteractionRules;
import rinde.sim.core.simulation.time.TimeLapseHandle;

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

	private final HashMap<Address, CommGuard> fullComms = Maps.newHashMap();
	private final HashMap<Address, SimpleCommGuard> simpleComms = Maps.newHashMap();
	
	private List<SimpleCommGuard> activeGuards = Lists.newArrayList();
	
	private int nextId = 0;
	private RandomGenerator rnd;
	
	/**
	 * Create a new communication model.
	 */
	public CommunicationModel(){
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
	    SimpleCommGuard receiver;
	    if(simpleComms.containsKey(destination))
	        receiver = simpleComms.get(destination);
	    else
	        receiver = fullComms.get(destination);
	    
	    synchronized(receiver){
	        synchronized(this){
	            if(!receiver.isActive())
	                activeGuards.add(receiver);
	        }
    	    receiver.receive(msg);
	    }
	}
	
	/**
	 * Broadcast a message.
	 * @param msg The message to be broadcasted.
	 */
	public void broadcast(Delivery msg){
		CommGuard sender = fullComms.get(msg.sender);
		Point senderLocation = sender.getLastLocation();
		
		
		for(Entry<Address, CommGuard> e:fullComms.entrySet()){
		    Address a = e.getKey();
		    CommGuard g = e.getValue();
		    
			if( Point.distance(g.getLastLocation(),senderLocation) < sender.getRadius()){
				try {
			        synchronized(g){
			            synchronized(this){
			                if(!g.isActive())
			                    activeGuards.add(g);
			            }
			            g.receive(msg.clone());
			        }
				} catch (CloneNotSupportedException exc) {
					exc.printStackTrace();
				}
			}
		}
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
	    
	    if(user instanceof FullCommUser<?>){
	        CommGuard guard = new CommGuard((FullCommUser<?>) user, (CommData) data, this, rnd.nextLong(), handle);
	        ((FullCommUser<?>) user).setCommunicationAPI(guard); 
	        fullComms.put(guard.getAddress(), guard);
	    }
	    else if(user instanceof SimpleCommUser<?>){
	        SimpleCommGuard guard =
	                new SimpleCommGuard((SimpleCommUser<?>) user, (SimpleCommData) data, this, rnd.nextLong(), handle);
	        ((SimpleCommUser<?>) user).setCommunicationAPI(guard);
            simpleComms.put(guard.getAddress(), guard);
	    }
	    else {
            throw new IllegalArgumentException("unknown type received..");
	    }
        
        return Lists.newArrayList();
	}

	@Override
	public List<User<?>> unregister(CommUser<?> user) {
        assert user!=null : "User can not be null.";
        
        if(user instanceof FullCommUser<?>){
            Address a = ((FullCommUser<?>) user).getCommunicationState().getAddress();
            assert(fullComms.containsKey(a));
            fullComms.remove(a);
        }
        else if(user instanceof SimpleCommUser<?>){
            Address a = ((SimpleCommUser<?>) user).getCommunicationState().getAddress();
            assert(simpleComms.containsKey(a));
            simpleComms.remove(a);
        }
        else {
            throw new IllegalArgumentException("unknown type to unregister..");
        }

        return Lists.newArrayList();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
	public Class<CommUser<?>> getSupportedType() {
		return (Class) CommUser.class;
	}

    @Override
    public void tick(TimeInterval time) {
        for(SimpleCommGuard guard:activeGuards){
            guard.process();
        }
        
        //TODO
        /*final CountDownLatch latch = new CountDownLatch(activeGuards.size());
        
        for(final SimpleCommGuard guard:activeGuards){
            pool.submit(new Runnable() {
                @Override
                public void run() {
                    guard.process();
                    latch.countDown();
                }
            });
        }
        
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/
        
        activeGuards.clear();
    }
    
    @Override
    public void init(long seed, InteractionRules rules, TimeInterval masterTime) {
        this.rnd = new MersenneTwister(seed);
    }
}
