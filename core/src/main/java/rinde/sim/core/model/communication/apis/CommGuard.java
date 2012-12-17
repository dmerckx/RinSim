package rinde.sim.core.model.communication.apis;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import rinde.sim.FullGuard;
import rinde.sim.core.graph.Point;
import rinde.sim.core.model.Data;
import rinde.sim.core.model.User;
import rinde.sim.core.model.communication.Address;
import rinde.sim.core.model.communication.CommunicationModel;
import rinde.sim.core.model.communication.Delivery;
import rinde.sim.core.model.communication.Message;
import rinde.sim.core.model.communication.users.CommData;
import rinde.sim.core.model.communication.users.CommUser;
import rinde.sim.core.simulation.TimeInterval;
import rinde.sim.core.simulation.TimeLapse;

public class CommGuard extends CommunicationState implements CommunicationAPI, FullGuard, User<Data>{
	
	private List<Delivery> mailbox;
	private SortedSet<Delivery> tempMailbox;
	private Address address;
	private final CommunicationModel model;
	private final CommUser<?> user;
	
    private double lastRadius;
    private double radius;
    private double lastReliability;
    private double reliability;
	
	private boolean isChanged;
	
	public CommGuard(CommUser<?> user, CommData data, CommunicationModel model){
	    super();
		this.user = user;
		this.model = model;
		this.address = model.generateAddress();
		this.mailbox = new ArrayList<Delivery>();
		this.tempMailbox = new TreeSet<Delivery>();
		
		this.radius = data.getInitialRadius();
		this.lastRadius = radius;
        this.reliability = data.getInitialReliability();
        this.lastReliability = reliability;
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
		return null;
	    //return user;
	}
     
    public Point getLastLocation(){
        return user.getRoadState().getLocation();
        
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

    @Override
    public void setReliability(double reliability) {
        this.reliability = reliability;
        isChanged = true;
    }

    @Override
    public void setRadius(double radius) {
        this.radius = radius;
        isChanged = true;
    }

    @Override
    public CommunicationState getState() {
        return this;
    }

    // ------ FULL GUARD ----- //
    
    @Override
    public void tick(TimeLapse time) {}
    
    public void afterTick(TimeInterval l) {
        mailbox.addAll(tempMailbox);
        tempMailbox.clear();
        
        if(isChanged){
            lastRadius = radius;
            lastReliability = reliability;
            isChanged = false;
        }
    }
}
