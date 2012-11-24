package rinde.sim.core.model.communication.guards;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import rinde.sim.core.graph.Point;
import rinde.sim.core.model.communication.Address;
import rinde.sim.core.model.communication.CommunicationModel;
import rinde.sim.core.model.communication.Delivery;
import rinde.sim.core.model.communication.Message;
import rinde.sim.core.model.communication.apis.CommunicationAPI;
import rinde.sim.core.model.communication.supported.CommUnit;
import rinde.sim.core.model.communication.users.CommUser;
import rinde.sim.core.model.road.apis.RoadAPI;
import rinde.sim.core.simulation.TimeInterval;

public class CommGuard implements CommunicationAPI{
	
	private List<Delivery> mailbox;
	private SortedSet<Delivery> tempMailbox;
	private Address address;
	private final CommunicationModel model;
	private final CommUser user;
	
	private RoadAPI roadAPI;
	
    private double lastRadius;
    private double radius;
    private double lastReliability;
    private double reliability;
	
	private boolean isChanged;
	
	public CommGuard(CommUnit unit, CommunicationModel model){
		this.user = unit.getElement();
		this.model = model;
		this.address = model.generateAddress();
		this.mailbox = new ArrayList<Delivery>();
		this.tempMailbox = new TreeSet<Delivery>();
		
		this.radius = unit.getInitData().getInitialRadius();
		this.reliability = unit.getInitData().getInitialReliability();
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
	
    public void afterTick(TimeInterval l) {
        mailbox.addAll(tempMailbox);
        tempMailbox.clear();
        
        if(isChanged){
            lastRadius = radius;
            lastReliability = reliability;
            isChanged = false;
        }
    }
     
    public Point getLastLocation(){
        return roadAPI.getLastLocation();
        
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
}
