package rinde.sim.core.model.interaction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import rinde.sim.core.graph.Point;
import rinde.sim.core.model.Model;
import rinde.sim.core.model.SimulatorModelAPI;
import rinde.sim.core.model.interaction.guards.InteractiveGuard;
import rinde.sim.core.model.interaction.supported.InteractiveHolder;
import rinde.sim.core.model.interaction.supported.InteractiveReceiver;
import rinde.sim.core.model.interaction.supported.InteractiveType;
import rinde.sim.core.model.interaction.users.InteractiveAgent;
import rinde.sim.core.simulation.TimeLapse;

import com.google.common.collect.HashMultimap;


public class InteractionModel implements Model<InteractiveType> {
    
    private SimulatorModelAPI simAPI;
    
    private HashMultimap<Point, Receiver> receiversPos;
    
    private HashMap<InteractiveAgent, InteractiveGuard<?>> mapping;
    
    public InteractionModel() {
        receiversPos = HashMultimap.create();
        mapping = new HashMap<InteractiveAgent, InteractiveGuard<?>>();
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
    
    public <N extends Notification> void advertise(Receiver receiver){
        receiversPos.put(receiver.location, receiver);
    }
    
    public void remove(Receiver receiver){
        receiversPos.remove(receiver.location, receiver);
    }
    
    @Override
    public void setSimulatorAPI(SimulatorModelAPI api) {
        this.simAPI = api;
    }

    @Override
    public void register(InteractiveType element) {
        if(element instanceof InteractiveHolder){
            InteractiveHolder holder = (InteractiveHolder) element;
            mapping.put(holder.getElement(), holder.getInteractiveGuard());
        }
        else if(element instanceof InteractiveReceiver){
            advertise(((InteractiveReceiver) element).receiver);
        }
        else{
            throw new IllegalArgumentException(element.getClass() + " is not a supported type");
        }
    }

    @Override
    public void unregister(InteractiveType element) {
        if(element instanceof InteractiveHolder){
            //TODO
            throw new UnsupportedOperationException("Not implemented yet");
        }
        else if(element instanceof InteractiveReceiver){
            advertise(((InteractiveReceiver) element).receiver);
        }
        else{
            throw new IllegalArgumentException(element.getClass() + " is not a supported type");
        }
    }

    @Override
    public Class<InteractiveType> getSupportedType() {
        return InteractiveType.class;
    }
 
    public <N extends Notification> InteractiveGuard<N> makeGuard(InteractiveAgent agent){
        return new InteractiveGuard<N>(agent, this);
    }
}
