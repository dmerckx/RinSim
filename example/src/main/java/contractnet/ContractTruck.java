package contractnet;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;

import rinde.sim.core.graph.Point;
import rinde.sim.core.model.Agent;
import rinde.sim.core.model.communication.Address;
import rinde.sim.core.model.communication.Delivery;
import rinde.sim.core.model.communication.Message;
import rinde.sim.core.model.communication.apis.CommAPI;
import rinde.sim.core.model.communication.apis.CommunicationState;
import rinde.sim.core.model.communication.users.CommData;
import rinde.sim.core.model.communication.users.FullCommUser;
import rinde.sim.core.model.pdp.Parcel;
import rinde.sim.core.model.pdp.users.Truck;
import rinde.sim.core.model.pdp.users.TruckData;
import rinde.sim.core.simulation.TimeLapse;

import com.google.common.collect.Lists;

import contractnet.ContractTruck.CTTruckData;
import contractnet.messages.Accept;
import contractnet.messages.Auction;
import contractnet.messages.Bid;
import contractnet.messages.Proposal;
import contractnet.messages.Reject;

public class ContractTruck extends Truck<CTTruckData> implements FullCommUser<CTTruckData>, Agent, Comparator<Option>{

	public static final int CONSIDERATION_TIME = 5; 
	
	private CommAPI commAPI;
	
	private LinkedList<Option> proposals = Lists.newLinkedList();
	private int counter = 0;
	private Point target;
	private State state;
	
	private enum State{
		SEARCHING,
		CONDSIDERING,
		DRIVING_TO_PICKUP,
		DRIVING_TO_DELIVERY;
	}
	
	public ContractTruck() {
		state = State.SEARCHING;
	}
	
	@Override
	public CommunicationState getCommunicationState() {
		return commAPI.getState();
	}

	@Override
	public void setCommunicationAPI(CommAPI api) {
		this.commAPI = api;
	}
	
	@Override
	public void tick(TimeLapse time) {
		handleMail();
		handleState();
		drive(time);
	}
	
	private void handleMail(){
		Iterator<Delivery> messages = commAPI.getMessages();
		
		while(messages.hasNext()){
			Delivery d = messages.next();
			Message m = d.message;
			
			switch(state){
			case SEARCHING:
				if(m instanceof Proposal){
					changeState(State.CONDSIDERING, target == null? roadAPI.getRandomLocation():target);
				}
				//$FALL-THROUGH$
			case CONDSIDERING:
				if(m instanceof Auction){
					commAPI.send(d.sender, new Bid(calculateBid(((Auction) m).location)));
				}
				else if(m instanceof Proposal){
					proposals.add(new Option(d.sender, ((Proposal) m).location));
				}
				break;
			case DRIVING_TO_PICKUP:
				if(m instanceof Proposal){
					commAPI.send(d.sender, new Reject());
				}
				break;
			case DRIVING_TO_DELIVERY:
				if(m instanceof Auction){
					commAPI.send(d.sender, new Bid(calculateBid(((Auction) m).location)));
				}
				if(m instanceof Proposal){
					if(counter < 2)
						commAPI.send(d.sender, new Reject());
					else
						proposals.add(new Option(d.sender, ((Proposal) m).location));
				}
				break;
			}
			
			messages.remove();
		}
	}

	private void handleState() {
		counter++;

		if(state == State.CONDSIDERING){
			Collections.sort(proposals, this);
			
			if(counter >= CONSIDERATION_TIME){
				//System.out.println("NR OF PROPOSALS:" + proposals);
				Option bestOption = proposals.removeLast();
				//System.out.println("best: " + bestOption);
				
				//accept best option
				commAPI.send(bestOption.sender, new Accept());
				
				//refuse other options
				while(!proposals.isEmpty())
					commAPI.send(proposals.removeFirst().sender, new Reject());
	
				changeState(State.DRIVING_TO_PICKUP, bestOption.location);
			}
			else{
				roadAPI.setTarget(proposals.getLast().location);
			}
		}
	}
	
	private void drive(TimeLapse time){
		roadAPI.advance(time);	//Drive as far as possible
		
		if(roadAPI.isDriving() || !time.hasTimeLeft())
			return;
		
		switch(state){
		case SEARCHING:
			//$FALL-THROUGH$
		case CONDSIDERING:
			roadAPI.setTarget(roadAPI.getRandomLocation());
			break;
		case DRIVING_TO_PICKUP:
			Parcel pickedParcel = containerAPI.tryPickup(time);
			if(pickedParcel != null){
				changeState(State.DRIVING_TO_DELIVERY, pickedParcel.destination);
			}
			else{
				changeState(State.SEARCHING, roadAPI.getRandomLocation());
			}
			break;
		case DRIVING_TO_DELIVERY:
			Parcel deliveredParcel = containerAPI.tryDelivery(time);
			if(deliveredParcel == null) throw new IllegalStateException();
			
			if(proposals.isEmpty())
				changeState(State.SEARCHING, roadAPI.getRandomLocation());
			else 
				changeState(State.CONDSIDERING, roadAPI.getRandomLocation());
			break;
		}
	}
	
	private void changeState(State newState, Point target){
		this.state = newState;
		this.target = target;
		this.roadAPI.setTarget(target);
		this.counter = 0;
	}
	
	private Double calculateBid(Point to){
		if(state == State.DRIVING_TO_DELIVERY)
			return 1 / (Point.distance(roadAPI.getCurrentLocation(), target)
							+ Point.distance(target, to));
		else
			return 1 / (Point.distance(roadAPI.getCurrentLocation(), to));
	}
	
	public static class CTTruckData extends TruckData.Std implements CommData{
		private final double radius;
		
		public CTTruckData(double speed, Point pos, double cap, double radius) {
			super(speed, pos, cap);
			this.radius = radius;
		}

		@Override
		public Double getReliability() {
			return 1.0d;
		}

		@Override
		public Double getInitialRadius() {
			return radius;
		}
	}

	@Override
	public int compare(Option o1, Option o2) {
		return calculateBid(o1.location).compareTo(calculateBid(o2.location));
	}
}

class Option{
	public final Address sender;
	public final Point location;
	
	public Option(Address sender, Point location) {
		this.sender = sender;
		this.location = location;
	}
	
	@Override
	public String toString() {
		return "{" + sender.id + "}";
	}
}