package contractnet;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map.Entry;

import rinde.sim.core.model.Agent;
import rinde.sim.core.model.communication.Address;
import rinde.sim.core.model.communication.Delivery;
import rinde.sim.core.model.communication.Message;
import rinde.sim.core.model.communication.apis.CommAPI;
import rinde.sim.core.model.communication.apis.CommunicationState;
import rinde.sim.core.model.communication.users.CommData;
import rinde.sim.core.model.communication.users.FullCommUser;
import rinde.sim.core.model.pdp.Parcel;
import rinde.sim.core.model.pdp.users.PickupPoint;
import rinde.sim.core.model.pdp.users.PickupPointData;
import rinde.sim.core.simulation.TimeLapse;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import contractnet.ContractPickup.CTPickupData;
import contractnet.messages.Accept;
import contractnet.messages.Auction;
import contractnet.messages.Bid;
import contractnet.messages.Proposal;
import contractnet.messages.Reject;

public class ContractPickup extends PickupPoint<CTPickupData> implements FullCommUser<CTPickupData>,
							Agent, Comparator<Entry<Address, Double>>{

	public static final int REFRESH_RATE = 10;
	public static final int MAX_REJECTS = 2;
	
	private CommAPI commAPI;

	private HashMap<Address, Double> bids = Maps.newHashMap();
	private int rejections = 0;
	private State state;
	
	private enum State{
		AUCTIONING,
		WAITING,
		SOLD;
	}
	
	public ContractPickup() {
		this.state = State.AUCTIONING;
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
		handleState(time);
	}
	
	private void handleMail() {
		Iterator<Delivery> messages = commAPI.getMessages();
		
		while(messages.hasNext()){
			Delivery d = messages.next();
			Message m = d.message;
			
			switch(state){
			case AUCTIONING:
				if(m instanceof Bid){
					bids.put(d.sender, ((Bid) m).value);
				}
				break;
			case WAITING:
				if(m instanceof Accept){
					bids.clear();
					changeState(State.SOLD);
				}
				else if(m instanceof Reject){
					rejections++;
					changeState(State.AUCTIONING);
				}
				break;
			case SOLD:
				break;
			}
			
			messages.remove();
		}
	}
	
	private void handleState(TimeLapse time) {
		switch(state){
		case AUCTIONING:
			if(time.getStartTime() % REFRESH_RATE == 0)
				commAPI.broadcast(new Auction(roadAPI.getCurrentLocation()));
			
			if(rejections > MAX_REJECTS){
				bids.clear();
				rejections = 0;
				break;
			}
			
			if(bids.isEmpty()) break;
			
			LinkedList<Entry<Address, Double>> bidsList = Lists.newLinkedList(bids.entrySet());
			Collections.sort(bidsList, this);
			Entry<Address, Double> bestBid = bidsList.getLast();
			
			bids.remove(bestBid.getKey());
			commAPI.send(bestBid.getKey(), new Proposal(roadAPI.getCurrentLocation()));
			changeState(State.WAITING);
			
			/*if(counter > REFRESH_RATE){
				if(bids.isEmpty() || rejections > MAX_REJECTS){
					bids.clear();
					rejections = 0;
					counter = 0;
					changeState(State.BEFORE_AUCTION);
				}
				else{
					Collections.sort(bids, this);
					commAPI.send(bids.removeLast().sender, new Proposal(roadAPI.getCurrentLocation()));
					changeState(State.WAITING_FOR_WINNER);
				}
			}*/
			break;
		case WAITING:
			break;
		case SOLD:
			break;
		}
	}
	
	private void changeState(State newState){
		this.state = newState;
	}
	
	public static class CTPickupData extends PickupPointData.Std implements CommData{
		private final double radius;
		
		public CTPickupData(Parcel parcel, double radius) {
			super(parcel);
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
	public int compare(Entry<Address, Double> o1, Entry<Address, Double> o2) {
		return o1.getValue().compareTo(o2.getValue());
	}
	
	@Override
	public String toString() {
		return "pp" + (commAPI != null? commAPI.getAddress().id:"");
	}
}