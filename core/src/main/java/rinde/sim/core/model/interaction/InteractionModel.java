package rinde.sim.core.model.interaction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import rinde.sim.core.graph.Point;
import rinde.sim.core.model.Data;
import rinde.sim.core.model.Model;
import rinde.sim.core.model.User;
import rinde.sim.core.model.communication.CommunicationModel;
import rinde.sim.core.model.communication.users.SimpleCommData;
import rinde.sim.core.model.interaction.apis.InteractiveGuard;
import rinde.sim.core.model.interaction.users.InteractionUser;
import rinde.sim.core.simulation.TimeInterval;
import rinde.sim.core.simulation.TimeLapse;
import rinde.sim.core.simulation.UserInit;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * A model allowing its users to advertise {@link Receiver}s and visit
 * existing receivers by using {@link Visitor}s.
 * 
 * This model supports the following types:
 *  - {@link InteractionUser}: {@link Data}
 * 
 * @author dmerckx
 */
public class InteractionModel implements Model<Data, InteractionUser<?>> {
    
    private HashMultimap<Point, Receiver> receiversPos = HashMultimap.create();
    private HashMap<InteractionUser<?>, InteractiveGuard> mapping = Maps.newHashMap();
    
    private CommunicationModel commModel;
    
    /**
     * Create a new interaction model.
     * @param commModel The required communication model.
     */
    @SuppressWarnings("hiding")
    public InteractionModel(CommunicationModel commModel) {
        assert commModel != null;
        
        this.commModel = commModel;
    }
    
    /**
     * Perform an actual visit by a {@link Visitor}.
     * @param lapse The timelapse at which this visit takes place.
     * @param visitor The visitor.
     * @return The result of the visit.
     */
    @SuppressWarnings("unchecked")
    public <T extends Receiver, R extends Result> R visit(TimeLapse lapse, Visitor<T, R> visitor){
        List<T> targets = new ArrayList<T>();

        System.out.println("visit" + visitor.location);
        System.out.println("receivers for this pos: " + receiversPos.get(visitor.location));
        
        for(Receiver r: receiversPos.get(visitor.location)){
            if( visitor.target.isAssignableFrom(r.getClass())){
                targets.add((T) r);
            }
        }
        
        return visitor.visit(lapse, targets);
    }
    
    /**
     * Advertise a new receiver.
     * @param receiver The receiver to advertise.
     */
    public void advertise(Receiver receiver){
        assert !(receiver instanceof ExtendedReceiver);
        
        receiver.setModel(this);
        receiversPos.put(receiver.location, receiver);
    }
    
    /**
     * Advertise a new extended receiver. 
     * @param receiver The extended receiver to advertise.
     * @param guard The guard from which this receiver originates.
     */
    public void advertise(ExtendedReceiver receiver, InteractiveGuard guard){
        commModel.register(receiver, SimpleCommData.RELIABLE);
    }
    
    /**
     * Remove a receiver.
     * @param receiver The receiver to remove.
     */
    public void remove(Receiver receiver){
        receiversPos.remove(receiver.location, receiver);
        mapping.get(receiver).receiveTermination(receiver);
        
        if(receiver instanceof ExtendedReceiver){
            commModel.unregister((ExtendedReceiver) receiver);
        }
    }
    
    
    // ----- MODEL ----- //

    @Override
    public List<UserInit<?>> register(InteractionUser<?> user, Data d) {
        assert user!=null : "User can not be null.";
        
        InteractiveGuard guard = new InteractiveGuard(user, this);
        user.setInteractionAPi(guard);
        
        mapping.put(user, guard);
        
        List<UserInit<?>> result = Lists.newArrayList();
        result.add(UserInit.create(guard, SimpleCommData.RELIABLE));
        
        return result;
    }

    @Override
    public List<User<?>> unregister(InteractionUser<?> user) {
        assert user!=null : "User can not be null.";
        
        List<User<?>> result = Lists.newArrayList();
        result.add(mapping.get(user));
        
        for(Receiver r: mapping.get(user).receivers){
            if(r instanceof ExtendedReceiver){
                commModel.unregister((ExtendedReceiver) r);
            }
        }
        mapping.remove(user);
        
        return result;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public Class<InteractionUser<?>> getSupportedType() {
        return (Class) InteractionUser.class;
    }

    @Override
    public void tick(TimeInterval time) {
        
    }

    @Override
    public void setSeed(long seed) {
        
    }
}
