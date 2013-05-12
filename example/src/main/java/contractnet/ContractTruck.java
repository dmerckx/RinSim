package contractnet;

import java.util.Iterator;

import rinde.sim.core.graph.Point;
import rinde.sim.core.model.Agent;
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
import contractnet.ContractTruck.CTTruckData;
import contractnet.messages.Accept;
import contractnet.messages.Auction;
import contractnet.messages.Bid;
import contractnet.messages.Reject;
import contractnet.messages.Winner;

public class ContractTruck extends Truck<CTTruckData> implements FullCommUser<CTTruckData>, Agent {
	
	private CommAPI commAPI;
	
	private State state;
	
	private enum State{
		SEARCHING,
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
		drive(time);
	}
	
	private void handleMail(){
		Iterator<Delivery> messages = commAPI.getMessages();
		
		while(messages.hasNext()){
			Delivery d = messages.next();
			Message m = d.message;
			
			switch(state){
			case SEARCHING:
				if(m instanceof Auction){
					double bid = 1 / Point.distance(roadAPI.getCurrentLocation(), ((Auction) m).location);
					commAPI.send(d.sender, new Bid(bid));
				}
				else if(m instanceof Winner){
					commAPI.send(d.sender, new Accept());
					roadAPI.setTarget(((Winner) m).location);
					changeState(State.DRIVING_TO_PICKUP);
				}
				break;
			case DRIVING_TO_PICKUP:
			case DRIVING_TO_DELIVERY:
				if(m instanceof Winner){
					commAPI.send(d.sender, new Reject());
				}
				break;
			}
			
			messages.remove();
		}
	}
	
	private void drive(TimeLapse time){
		roadAPI.advance(time);	//Drive as far as possible
		
		if(roadAPI.isDriving() || !time.hasTimeLeft())
			return;
		
		switch(state){
		case SEARCHING:
			roadAPI.setTarget(roadAPI.getRandomLocation());
			break;
		case DRIVING_TO_PICKUP:
			Parcel pickedParcel = containerAPI.tryPickup(time);
			if(pickedParcel != null){
				roadAPI.setTarget(pickedParcel.destination);
				changeState(State.DRIVING_TO_DELIVERY);
			}
			else{
				changeState(State.SEARCHING);
			}
			break;
		case DRIVING_TO_DELIVERY:
			Parcel deliveredParcel = containerAPI.tryDelivery(time);
			if(deliveredParcel == null) throw new IllegalStateException();
			changeState(State.SEARCHING);
			break;
		}
		
	}
	
	private void changeState(State newState){
		this.state = newState;
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
}
