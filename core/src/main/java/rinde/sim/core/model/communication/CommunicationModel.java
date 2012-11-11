package rinde.sim.core.model.communication;

import java.util.HashMap;

import rinde.sim.core.graph.Point;
import rinde.sim.core.model.Model;
import rinde.sim.core.model.SimulatorModelAPI;
import rinde.sim.core.simulation.TimeInterval;
import rinde.sim.core.simulation.TimeLapse;

public class CommunicationModel implements Model<CommunicationUser> {

	private final HashMap<Address, CommunicationPort> comms;
	private int nextId = 0;
	private SimulatorModelAPI api;
	
	public CommunicationModel(){
		comms = new HashMap<Address, CommunicationPort>();
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
        
		CommunicationPort sender = comms.get(msg.address);
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
	public void register(CommunicationUser element) {
		CommunicationPort comm = new CommunicationPort(element, this);
		element.setCommunicationAPI(comm);
		api.registerPort(comm);
		comms.put(comm.getAddress(), comm);
	}

	@Override
	public void unregister(CommunicationUser element) {
		for(Address a: comms.keySet()){
			if( comms.get(a).getUser() == element ){
				api.unregisterPort(comms.get(a));
				comms.remove(a);
				return;
			}
		}
	}

	@Override
	public Class<CommunicationUser> getSupportedType() {
		return CommunicationUser.class;
	}

	@Override
	public void preTick(TimeInterval l) {
		// do nothing
	}
}
