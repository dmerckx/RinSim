package rinde.sim.core.model.communication;

import java.util.HashMap;

import rinde.sim.core.graph.Point;
import rinde.sim.core.model.Model;
import rinde.sim.core.model.communication.guards.CommGuard;
import rinde.sim.core.model.communication.supported.CommUnit;
import rinde.sim.core.simulation.TimeInterval;

public class CommunicationModel implements Model<CommUnit>{

	private final HashMap<Address, CommGuard> comms;
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
	public void register(CommUnit unit) {
        CommGuard guard = new CommGuard(unit, this);
	    unit.setCommunicationAPI(guard);
	    
		comms.put(guard.getAddress(), guard);
	}

	@Override
	public void unregister(CommUnit element) {
		for(Address a: comms.keySet()){
			if( comms.get(a).getUser() == element ){
				comms.remove(a);
				return;
			}
		}
	}

	@Override
	public Class<CommUnit> getSupportedType() {
		return CommUnit.class;
	}

    @Override
    public void tick(TimeInterval time) {
        
    }
}
