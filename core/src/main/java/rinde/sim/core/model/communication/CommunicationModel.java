package rinde.sim.core.model.communication;

import java.util.HashMap;
import java.util.List;

import rinde.sim.core.graph.Point;
import rinde.sim.core.model.Model;
import rinde.sim.core.model.User;
import rinde.sim.core.model.communication.apis.CommGuard;
import rinde.sim.core.model.communication.users.CommData;
import rinde.sim.core.model.communication.users.CommUser;
import rinde.sim.core.simulation.TimeInterval;
import rinde.sim.core.simulation.UserInit;

import com.google.common.collect.Lists;

public class CommunicationModel implements Model<CommData, CommUser<?>>{

	protected final HashMap<Address, CommGuard> comms;
	private int nextId = 0;
	
	public CommunicationModel(){
		comms = new HashMap<Address, CommGuard>();
	}
	
	public Address generateAddress(){
		return new Address(nextId++);
	}
	
	public void send(Address destination, Delivery msg){
		comms.get(destination).receive(msg);
	}
	
	public void broadcast(Delivery msg){
		CommGuard sender = comms.get(msg.address);
		Point senderLocation = sender.getLastLocation();
		
		for(Address a: comms.keySet()){
			if( Point.distance(comms.get(a).getLastLocation(),senderLocation) < sender.getRadius()){
				try {
					comms.get(a).receive(msg.clone());
				} catch (CloneNotSupportedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	
	// ----- MODEL ----- //

	@Override
	public List<UserInit<?>> register(CommUser<?> user, CommData data) {
        assert user!=null : "User can not be null.";
	    assert data!=null : "Data can not be null.";
	    
        CommGuard guard = new CommGuard(user, data, this);
	    user.SetCommunicationAPI(guard);
	    
		comms.put(guard.getAddress(), guard);

		List<UserInit<?>> result = Lists.newArrayList();
        result.add(UserInit.create(guard));
        
        return result;
	}

	@Override
	public List<User<?>> unregister(CommUser<?> user) {
        assert user!=null : "User can not be null.";
	   
        Address a = user.getCommunicationState().getAddress();
        
        assert comms.containsKey(a);
        
        List<User<?>> result = Lists.newArrayList();
        result.add(comms.get(a));
        
        comms.remove(a);
        
        return result;
	}

	@Override
	public Class<CommUser<?>> getSupportedType() {
		return (Class) CommUser.class;
	}

    @Override
    public void tick(TimeInterval time) {
        
    }
}
