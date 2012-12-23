package rinde.sim.core.model.interaction.apis;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import rinde.sim.core.model.communication.Delivery;
import rinde.sim.core.model.communication.Message;
import rinde.sim.core.model.communication.apis.CommunicationState;
import rinde.sim.core.model.communication.apis.SimpleCommAPI;
import rinde.sim.core.model.communication.users.SimpleCommData;
import rinde.sim.core.model.communication.users.SimpleCommUser;
import rinde.sim.core.model.interaction.ExtendedReceiver;
import rinde.sim.core.model.interaction.InteractionModel;
import rinde.sim.core.model.interaction.Receiver;
import rinde.sim.core.model.interaction.Result;
import rinde.sim.core.model.interaction.Visitor;
import rinde.sim.core.model.interaction.users.InteractionUser;
import rinde.sim.core.simulation.TimeLapse;

import com.google.common.collect.Lists;

/**
 * An implementation of the {@link InteractionAPI}.
 * 
 * This guard guarantees consistency for undergoing interactions:
 *  - new notifications are received in a thread safe way
 *  - new notifications are not shown to the user until the next turn
 *  - advertised receivers are activated after everyone processed its tick
 *  - using a visitor forces a sleep until all users before this user 
 *  have finished their turn, 2 visits are never processed simultaneously
 * 
 * @author dmerckx
 */
public class InteractiveGuard implements SimpleCommUser<SimpleCommData>, InteractionAPI{

    private final InteractionModel interactionModel;
    private SimpleCommAPI commAPI;
    
    /**
     * The list with active receivers that were advertised by this guard.
     */
    public final List<Receiver> receivers = new ArrayList<Receiver>();
    
    /**
     * Construct a new guard. 
     * @param user The user to which this API belongs.
     * @param model The interaction model. 
     */
    public InteractiveGuard(InteractionUser<?> user, InteractionModel model) {
        this.interactionModel = model;
    }
    
    
    // ----- SIMPLE COMM USER ----- // 
    
    @Override
    public void setCommunicationAPI(SimpleCommAPI api) {
        this.commAPI = api;
    }

    @Override
    public CommunicationState getCommunicationState() {
        return commAPI.getState();
    }
    
    /**
     * Receive a message that one of the receivers advertised by this guard was
     * terminated;
     * @param receiver The receiver that terminated
     */
    public synchronized void receiveTermination(Receiver receiver){
        receivers.remove(receiver);
    }
    
    @Override
    public List<Message> getNotifications(){
        Iterator<Delivery> it = commAPI.getMessages();
        
        List<Message> result = Lists.newArrayList();
        
        while(it.hasNext()){
            Delivery d = it.next();
            result.add(d.message);
            it.remove();
        }
        
        return result;
    }
    
    @Override
    public <R extends Result> R visit(TimeLapse lapse, Visitor<?, R> visitor){
        return interactionModel.visit(lapse, visitor);
    }
    
    @Override
    public void advertise(Receiver receiver){
        if(receiver instanceof ExtendedReceiver){
            ((ExtendedReceiver) receiver).setGuard(commAPI.getAddress());
        }
        receivers.add(receiver);
        interactionModel.advertise(receiver);
    }

    @Override
    public void removeAll(Class<?> target) {
        Iterator<Receiver> it = receivers.iterator();
        while(it.hasNext()){
            Receiver receiver = it.next();
            if( receiver.getClass().isAssignableFrom(target)){
                interactionModel.remove(receiver);
                it.remove();
            }
        }
    }

    @Override
    public void removeAll() {
        for(Receiver receiver:Lists.newArrayList(receivers)){
            interactionModel.remove(receiver);
        }
        
        assert receivers.isEmpty() : "all receivers should be removed at this point";
    }
}
