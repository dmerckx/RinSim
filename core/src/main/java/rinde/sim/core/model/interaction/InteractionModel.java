package rinde.sim.core.model.interaction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import rinde.sim.core.graph.Point;
import rinde.sim.core.model.Data;
import rinde.sim.core.model.Model;
import rinde.sim.core.model.User;
import rinde.sim.core.model.interaction.apis.InteractiveGuard;
import rinde.sim.core.model.interaction.users.InteractionUser;
import rinde.sim.core.simulation.TimeInterval;
import rinde.sim.core.simulation.TimeLapse;
import rinde.sim.core.simulation.UserInit;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;


public class InteractionModel implements Model<Data, InteractionUser<?>> {
    
    private HashMultimap<Point, Receiver> receiversPos;
    
    private HashMap<InteractionUser, InteractiveGuard> mapping;
    
    public InteractionModel() {
        receiversPos = HashMultimap.create();
        mapping = new HashMap<InteractionUser, InteractiveGuard>();
    }
    
    public <T extends ExtendedReceiver, R extends Result> R visit(TimeLapse lapse, Visitor<T, R> visitor){
        List<T> targets = new ArrayList<T>();
        
        for(Receiver r: receiversPos.get(visitor.location)){
            if( r.getClass().isAssignableFrom(visitor.target)){
                targets.add((T) r);
            }
        }
        
        return visitor.visit(lapse, targets);
    }
    
    public void advertise(Receiver receiver){
        receiversPos.put(receiver.location, receiver);
    }
    
    public void advertise(ExtendedReceiver receiver, InteractiveGuard guard){
        receiver.setGuard(guard);
    }
    
    public void remove(Receiver receiver){
        receiversPos.remove(receiver.location, receiver);
    }
    
    
    // ----- MODEL ----- //

    @Override
    public List<UserInit<?>> register(InteractionUser<?> user, Data d) {
        assert user!=null : "User can not be null.";
        
        InteractiveGuard guard = new InteractiveGuard(user, this);
        user.setInteractionAPi(guard);
        
        mapping.put(user, guard);
        
        List<UserInit<?>> result = Lists.newArrayList();
        result.add(UserInit.create(guard));
        
        return result;
    }

    @Override
    public List<User<?>> unregister(InteractionUser<?> user) {
        assert user!=null : "User can not be null.";
        
        List<User<?>> result = Lists.newArrayList();
        result.add(mapping.get(user));
        
        mapping.remove(user);
        
        return result;
    }

    @Override
    public Class<InteractionUser<?>> getSupportedType() {
        return (Class) InteractionUser.class;
    }

    @Override
    public void tick(TimeInterval time) {
        
    }
}
