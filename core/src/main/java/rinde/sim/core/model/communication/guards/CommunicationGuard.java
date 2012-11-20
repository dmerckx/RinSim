package rinde.sim.core.model.communication.guards;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import rinde.sim.core.graph.Point;
import rinde.sim.core.model.Guard;
import rinde.sim.core.model.communication.Address;
import rinde.sim.core.model.communication.CommunicationModel;
import rinde.sim.core.model.communication.Delivery;
import rinde.sim.core.model.communication.Message;
import rinde.sim.core.model.communication.apis.CommunicationAPI;
import rinde.sim.core.model.communication.users.CommUser;
import rinde.sim.core.refs.Ref;
import rinde.sim.core.refs.RefBackup;
import rinde.sim.core.refs.UpdateListener;
import rinde.sim.core.simulation.TimeInterval;

public class CommunicationGuard implements CommunicationAPI, Guard, UpdateListener{
	
	private List<Delivery> mailbox;
	private SortedSet<Delivery> tempMailbox;
	private Address address;
	private final CommunicationModel model;
	private final CommUser user;
	
	private RefBackup<Point> position;
	private Ref<Double> radius;
    private double lastRadius;
	private Ref<Double> reliability;
    private double lastReliability;
	
	private boolean isChanged;
	
	public CommunicationGuard(CommUser user, CommunicationModel model){
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
		this.lastRadius = radius.getValue();
		this.reliability = reliability;
		this.lastReliability = reliability.getValue();
		
		radius.addListener(this);
		reliability.addListener(this);
	}
	
	public Point getLocation(){
		return position.getLastValue();
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

	public CommUser getUser() {
		return user;
	}
	
    public void afterTick(TimeInterval l) {
        mailbox.addAll(tempMailbox);
        tempMailbox.clear();
        
        if(isChanged){
            lastRadius = radius.getValue();
            lastReliability = reliability.getValue();
            isChanged = false;
        }
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
