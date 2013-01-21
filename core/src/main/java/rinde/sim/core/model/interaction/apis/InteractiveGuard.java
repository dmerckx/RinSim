package rinde.sim.core.model.interaction.apis;

import java.util.Iterator;
import java.util.List;

import rinde.sim.core.model.communication.Delivery;
import rinde.sim.core.model.communication.Message;
import rinde.sim.core.model.communication.apis.CommunicationState;
import rinde.sim.core.model.communication.apis.SimpleCommAPI;
import rinde.sim.core.model.communication.users.SimpleCommData;
import rinde.sim.core.model.communication.users.SimpleCommUser;
import rinde.sim.core.model.interaction.InteractionModel;
import rinde.sim.core.model.interaction.Receiver;
import rinde.sim.core.model.interaction.Result;
import rinde.sim.core.model.interaction.Visitor;
import rinde.sim.core.model.interaction.users.InteractionUser;
import rinde.sim.core.simulation.TimeLapse;
import rinde.sim.core.simulation.time.TimeLapseHandle;

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
public class InteractiveGuard implements InteractionAPI{

    private final InteractionModel interactionModel;
    private SimpleCommAPI commAPI;
    
    private long terminationTime = -1;
    
    private final TimeLapseHandle handle;
    
    private Receiver receiver = null;
    
    /**
     * Construct a new guard. 
     * @param user The user to which this API belongs.
     * @param model The interaction model. 
     * @param handle A handle to the users time lapse.
     */
    @SuppressWarnings("hiding")
    public InteractiveGuard(InteractionUser<?> user, InteractionModel model, TimeLapseHandle handle) {
        this.interactionModel = model;
        this.handle = handle;
    }
    
    /**
     * Called at the end of a tick to unset the current advertised receiver.
     * @param extraTime The extra time to subtract.
     */
    public void unsetReceiver(long extraTime){
        assert receiver != null;
        receiver = null;
        handle.unblock(extraTime);
        terminationTime = handle.getEndTime();
    }


    public Receiver getReceiver() {
        return receiver;
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
    public void advertise(Receiver rec){
        //Cannot add 2 receivers in the same turn
        if(receiver != null)
            return;
        
        rec.setModel(interactionModel);
        interactionModel.schedualAdd(rec, this);
        handle.block();
        receiver = rec; 
    }

    @Override
    public boolean isAdvertising() {
        return receiver != null;
    }

    @Override
    public void stopAdvertising() {
        assert receiver != null;
        interactionModel.schedualRemove(receiver);
    }

    @Override
    public long getTerminationTime() {
        return terminationTime;
    }
}
