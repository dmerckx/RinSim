package rinde.sim.core.model.communication;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import rinde.sim.core.graph.Point;
import rinde.sim.core.refs.Ref;
import rinde.sim.core.refs.RefBackup;
import rinde.sim.core.refs.UpdateListener;
import rinde.sim.core.simulation.TimeInterval;
import rinde.sim.core.simulation.types.AgentPort;

class CommunicationPort implements CommunicationAPI, AgentPort, UpdateListener{
	
	private List<Delivery> mailbox;
	private SortedSet<Delivery> tempMailbox;
	private Address address;
	private final CommunicationModel model;
	private final CommunicationUser user;
	
	private Point lastLocation;
	private double lastRadius;
	private double lastReliability;
	
	private RefBackup<Point> position;
	private Ref<Double> radius;
	private Ref<Double> reliability;
	
	private boolean isChanged;
	
	public CommunicationPort(CommunicationUser user, CommunicationModel model){
		this.user = user;
		this.model = model;
		this.address = model.generateAddress();
		this.mailbox = new ArrayList<Delivery>();
		this.tempMailbox = new TreeSet<Delivery>(); 
	}

	@Override
	public void init(RefBackup<Point> pos, Ref<Double> radius, Ref<Double> reliability) {
		this.position = pos;
		this.radius = radius;
		this.reliability = reliability;
	}
	
	public Point getLocation(){
		return lastLocation;
	}
	
	public double getRadius(){
		return lastRadius;
	}
	
	public double getReliability(){
		return lastReliability;
	}
	
    public final synchronized void receive(Delivery delivery){
    	tempMailbox.add(delivery);
    }

	public CommunicationUser getUser() {
		return user;
	}
    
    // ----- PORT -----

    @Override
    public void tick(TimeInterval l) {
        mailbox.addAll(tempMailbox);
        tempMailbox.clear();
    }
	
    
    // ----- UPDATE LISTENER -----

    @Override
    public void notifyUpdate() {
        isChanged = true;
    }
    
    
    // ----- COMMUNICATION API -----

	@Override
	public void send(Address destination, Message message) {
		model.send(destination, new Delivery(address, message));
	}

	@Override
	public void broadcast(Message message) {
		model.broadcast(new Delivery(address, message));
	}

	@Override
	public Iterator<Delivery> getMessages() {
		return mailbox.iterator();
	}

	@Override
	public Address getAddress() {
		return address;
	}
}
