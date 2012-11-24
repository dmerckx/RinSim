package rinde.sim.core.model.interaction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import rinde.sim.core.graph.Point;
import rinde.sim.core.model.Model;
import rinde.sim.core.model.interaction.guards.InteractiveGuard;
import rinde.sim.core.model.interaction.supported.InteractiveUnit;
import rinde.sim.core.model.interaction.users.InteractiveUser;
import rinde.sim.core.simulation.TimeInterval;
import rinde.sim.core.simulation.TimeLapse;

import com.google.common.collect.HashMultimap;


public class InteractionModel implements Model<InteractiveUnit> {
    
    private HashMultimap<Point, Receiver> receiversPos;
    
    private HashMap<InteractiveUser, InteractiveGuard> mapping;
    
    public InteractionModel() {
        receiversPos = HashMultimap.create();
        mapping = new HashMap<InteractiveUser, InteractiveGuard>();
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
    public void register(InteractiveUnit unit) {
        InteractiveGuard guard = new InteractiveGuard(unit.getElement(), this);
        unit.setInteractiveAPI(guard);
    }

    @Override
    public void unregister(InteractiveUnit unit) {
        mapping.remove(unit.getElement());
    }

    @Override
    public Class<InteractiveUnit> getSupportedType() {
        return InteractiveUnit.class;
    }

    @Override
    public void tick(TimeInterval time) {
        
    }
}
