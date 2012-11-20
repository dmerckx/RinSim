package rinde.sim.core.model.communication;

import java.util.HashMap;

import rinde.sim.core.graph.Point;
import rinde.sim.core.model.Model;
import rinde.sim.core.model.SimulatorModelAPI;
import rinde.sim.core.model.communication.guards.CommunicationGuard;
import rinde.sim.core.model.communication.supported.CommunicationHolder;
import rinde.sim.core.model.communication.users.CommUser;

public class CommunicationModel implements Model<CommunicationHolder>{

	private final HashMap<Address, CommunicationGuard> comms;
	private int nextId = 0;
	private SimulatorModelAPI api;
	
	public CommunicationModel(){
		comms = new HashMap<Address, CommunicationGuard>();
	}

	@Override
	public void setSimulatorAPI(SimulatorModelAPI api) {
		this.api = api;
	}
	
	public Address generateAddress(){
		return new Address(nextId++);
	}
	
	public void send(Address destination, Delivery msg){
		comms.get(destination).receive(msg);
	}
	
	public void broadcast(Delivery msg){
        
		CommunicationGuard sender = comms.get(msg.address);
		Point senderLocation = sender.getLocation();
		
		for(Address a: comms.keySet()){
			if( Point.distance(comms.get(a).getLocation(),senderLocation) < sender.getRadius()){
				try {
					comms.get(a).receive(msg.clone());
				} catch (CloneNotSupportedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public void register(CommunicationHolder holder) {
	    CommUser element = holder.getElement();
		CommunicationGuard comm = holder.getCommunicationGuard();
		element.setCommunicationAPI(comm);
		api.registerGuard(comm);
		comms.put(comm.getAddress(), comm);
	}

	@Override
	public void unregister(CommunicationHolder element) {
		for(Address a: comms.keySet()){
			if( comms.get(a).getUser() == element ){
				api.unregisterGuard(comms.get(a));
				comms.remove(a);
				return;
			}
		}
	}

	@Override
	public Class<CommunicationHolder> getSupportedType() {
		return CommunicationHolder.class;
	}
}
