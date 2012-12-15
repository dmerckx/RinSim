package rinde.sim.core.model.communication;

import java.util.HashMap;

import rinde.sim.core.graph.Point;
import rinde.sim.core.model.Model;
import rinde.sim.core.model.communication.apis.CommGuard;
import rinde.sim.core.model.communication.users.CommData;
import rinde.sim.core.model.communication.users.CommUser;
import rinde.sim.core.model.road.RoadModel;
import rinde.sim.core.model.road.apis.RoadAPI;
import rinde.sim.core.simulation.SimulatorToModelAPI;
import rinde.sim.core.simulation.TimeInterval;

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
	public void register(SimulatorToModelAPI sim, CommUser<?> user, CommData data) {
        assert sim!=null: "Sim can not be null.";
        assert user!=null : "User can not be null.";
	    assert data!=null : "Data can not be null.";
	    
        CommGuard guard = new CommGuard(user, data, this, sim.getApi(user, RoadAPI.class));
	    user.SetCommunicationAPI(guard);
	    
	    sim.registerUser(guard);
	    
		comms.put(guard.getAddress(), guard);
	}

	@Override
	public void unregister(CommUser<?> user) {
        assert user!=null : "User can not be null.";
	   
	}

	@Override
	public Class<CommUser<?>> getSupportedType() {
		return (Class) CommUser.class;
	}

    @Override
    public void tick(TimeInterval time) {
        
    }
}
