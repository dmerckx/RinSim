package rinde.sim.core.model.interaction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.SortedSet;

import rinde.sim.core.graph.Point;
import rinde.sim.core.model.Data;
import rinde.sim.core.model.Model;
import rinde.sim.core.model.interaction.apis.InteractionGuard;
import rinde.sim.core.model.interaction.users.InteractionUser;
import rinde.sim.core.simulation.TimeInterval;
import rinde.sim.core.simulation.TimeLapse;
import rinde.sim.core.simulation.UserInit;
import rinde.sim.core.simulation.policies.InteractionRules;
import rinde.sim.core.simulation.time.TimeLapseHandle;
import rinde.sim.util.Tuple;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

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
    
    private LinkedHashMultimap<Point, Receiver> receiversPos = LinkedHashMultimap.create();
    private HashMap<InteractionUser<?>, InteractionGuard> mapping = Maps.newHashMap();
    
    private SortedSet<Receiver> schedualedForAdd = Sets.newTreeSet();
    private List<Receiver> schedualedRemoval = Lists.newArrayList();
    private List<Tuple<Receiver,Long>> schedualedTermination = Lists.newArrayList();
    
    private int guardId = 0;
    
    private InteractionRules interactionRules;
    
    //private double avgInteractions = 0;
    //private int interactions = 0;
    
    /**
     * Create a new interaction model.
     */
    public InteractionModel() {
        
    }
    
    /**
     * Perform an actual visit by a {@link Visitor}.
     * @param lapse The timelapse at which this visit takes place.
     * @param visitor The visitor.
     * @return The result of the visit.
     */
    @SuppressWarnings("unchecked")
    public <T extends Receiver, R extends Result> R visit(TimeLapse lapse, Visitor<T, R> visitor){
        //interactions++;
        interactionRules.awaitAllPrevious();
        
        List<T> targets = new ArrayList<T>();
        
        for(Receiver r: receiversPos.get(visitor.location)){
            if( visitor.target.isAssignableFrom(r.getClass())){
                targets.add((T) r);
            }
        }
        
        return visitor.visit(lapse, targets);
    }
    
    /**
     * Terminate a receiver, along with the time at which this occured.
     * @param receiver The receiver to be terminated.
     * @param time The time at which the receiver terminates.
     */
    public synchronized void terminate(Receiver receiver, long time){
        schedualedTermination.add(Tuple.create(receiver, time));
    }
    
    /**
     * Schedule this receiver for removal.
     * It will be removed at the end of that tick.
     * @param receiver The receiver to schedule.
     */
    public synchronized void schedualRemove(Receiver receiver){
        schedualedRemoval.add(receiver);
    }
    
    /**
     * Schedule this receiver to be added. It will be added at the end of thatsSatisfied();
     * tick.
     * @param receiver The receiver to add.
     * @param guard The guard that created the guard.
     */
    public synchronized void schedualAdd(Receiver receiver, InteractionGuard guard){
        receiver.model = this;
        receiver.guard = guard;
        
        schedualedForAdd.add(receiver);
    }
    
    
    // ----- MODEL ----- //

    @Override
    public List<UserInit<?>> register(InteractionUser<?> user, Data d, TimeLapseHandle handle) {
        assert user!=null : "User can not be null.";
        
        InteractionGuard guard = new InteractionGuard(user, this, handle, guardId++);
        user.setInteractionAPi(guard);
        
        mapping.put(user, guard);
        
        List<UserInit<?>> result = Lists.newArrayList();
        
        return result;
    }

    @Override
    public void unregister(InteractionUser<?> user) {
        assert user!=null : "User can not be null.";
        assert mapping.containsKey(user);
        
        mapping.remove(user);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public Class<InteractionUser<?>> getSupportedType() {
        return (Class) InteractionUser.class;
    }

    @Override
    public void tick(TimeInterval time) {
        double timesteps = time.getStartTime() / time.getTimeStep();
        //avgInteractions = interactions / timesteps;
        
        
        for(Receiver receiver:schedualedForAdd){
            receiversPos.put(receiver.location, receiver);
        }
        schedualedForAdd.clear();
        
        for(Tuple<Receiver, Long> entry:schedualedTermination){
            if(schedualedRemoval.contains(entry.getKey()))
                schedualedRemoval.remove(entry.getKey());
            receiversPos.remove(entry.getKey().location, entry.getKey());
            entry.getKey().guard.unsetReceiver(entry.getValue());
        }
        schedualedTermination.clear();
        
        for(Receiver receiver:schedualedRemoval){
            receiversPos.remove(receiver.location, receiver);
            receiver.guard.unsetReceiver(time.getEndTime());
        }
        schedualedRemoval.clear();
    }

    @Override
    public void init(long seed, InteractionRules rules, TimeInterval masterTime) {
        this.interactionRules = rules;
    }
    
    /*public double getAverageInteractions(){
        return avgInteractions;
    }*/
}