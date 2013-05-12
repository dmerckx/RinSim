package contractnet;

import java.util.Iterator;
import java.util.Map;
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

import com.google.common.collect.Maps;

import contractnet.ContractPickup.CTPickupData;
import contractnet.messages.Accept;
import contractnet.messages.Auction;
import contractnet.messages.Bid;
import contractnet.messages.Reject;
import contractnet.messages.Winner;

public class ContractPickup extends PickupPoint<CTPickupData> implements FullCommUser<CTPickupData>, Agent{

	public static final int REFRESH_RATE = 20;
	
	private CommAPI commAPI;

	private Map<Address, Double> bids = Maps.newLinkedHashMap();
	private State state;
	private int counter;
	
	private enum State{
		BEFORE_AUCTION,
		AUCTIONING,
		WAITING_FOR_WINNER,
		SOLD;
	}
	
	public ContractPickup() {
		this.state = State.BEFORE_AUCTION;
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
	}
	
	private void handleMail() {
		Iterator<Delivery> messages = commAPI.getMessages();
		
		while(messages.hasNext()){
			Delivery d = messages.next();
			Message m = d.message;
			
			switch(state){
			case BEFORE_AUCTION:
				break;
			case AUCTIONING:
				if(m instanceof Bid){
					bids.put(d.sender, ((Bid) m).value);
				}
				break;
			case WAITING_FOR_WINNER:
				if(m instanceof Accept){
					bids.clear();
					changeState(State.SOLD);
				}
				else if(m instanceof Reject){
					bids.remove(d.sender);
					changeState(State.AUCTIONING);
				}
				break;
			case SOLD:
				break;
			}
			
			messages.remove();
		}
	}
	
	private void handleState() {
		counter++;
		
		switch(state){
		case BEFORE_AUCTION:
			commAPI.broadcast(new Auction(roadAPI.getCurrentLocation()));
			changeState(State.AUCTIONING);
			break;
		case AUCTIONING:
			if(counter > REFRESH_RATE){
				if(bids.isEmpty()){
					changeState(State.BEFORE_AUCTION);
				}
				else{
					Address winner = null;
					double highest = 0;
					
					for(Entry<Address, Double> e:bids.entrySet()){
						if(highest < e.getValue())
							winner = e.getKey();
					}
					commAPI.send(winner, new Winner(roadAPI.getCurrentLocation()));
					changeState(State.WAITING_FOR_WINNER);
				}
			}
			break;
		case WAITING_FOR_WINNER:
			break;
		case SOLD:
			break;
		}
	}
	
	private void changeState(State newState){
		this.state = newState;
		this.counter = 0;
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
}
