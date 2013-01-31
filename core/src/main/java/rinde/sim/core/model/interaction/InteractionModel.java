package rinde.sim.core.model.interaction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

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
import rinde.sim.core.simulation.policies.ParallelExecution;
import rinde.sim.core.simulation.time.TimeLapseHandle;

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
    
    private List<Receiver> schedualedForAdd = Lists.newArrayList();
    private List<Receiver> schedualedRemoval = Lists.newArrayList();
    private HashMap<Receiver, Long> terminated = Maps.newHashMap();
    
    private HashMap<Receiver, InteractiveGuard> recGuards = Maps.newHashMap();
    
    /**
     * Create a new interaction model.
     * @param commModel The required communication model.
     */
    @SuppressWarnings("hiding")
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
        ParallelExecution.awaitAllPrevious();
        
        List<T> targets = new ArrayList<T>();
        
        for(Receiver r: receiversPos.get(visitor.location)){
            if( visitor.target.isAssignableFrom(r.getClass())){
                targets.add((T) r);
            }
        }
        
        return visitor.visit(lapse, targets);
    }
    
    public synchronized void terminate(Receiver receiver, long timeCost){
        terminated.put(receiver, timeCost);
    }
    
    /**
     * Schedule this receiver for removal. It will be removed at the end of that
     * tick.
     * @param receiver The receiver to schedule.
     * @param timeCost The time cost to apply when removing.
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
    public synchronized void schedualAdd(Receiver receiver, InteractiveGuard guard){
        recGuards.put(receiver, guard);
        schedualedForAdd.add(receiver);
    }
    
    
    // ----- MODEL ----- //

    @Override
    public List<UserInit<?>> register(InteractionUser<?> user, Data d, TimeLapseHandle handle) {
        assert user!=null : "User can not be null.";
        
        InteractiveGuard guard = new InteractiveGuard(user, this, handle);
        user.setInteractionAPi(guard);
        
        mapping.put(user, guard);
        
        List<UserInit<?>> result = Lists.newArrayList();
        
        return result;
    }

    @Override
    public List<User<?>> unregister(InteractionUser<?> user) {
        assert user!=null : "User can not be null.";
        
        List<User<?>> result = Lists.newArrayList();
        
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
        for(Receiver receiver:schedualedForAdd){
            receiversPos.put(receiver.location, receiver);
        }
        schedualedForAdd.clear();
        
        for(Entry<Receiver, Long> entry:terminated.entrySet()){
            if(schedualedRemoval.contains(entry.getKey()))
                schedualedRemoval.remove(entry.getKey());
            receiversPos.remove(entry.getKey().location, entry.getKey());
            recGuards.get(entry.getKey()).unsetReceiver(entry.getValue());
            recGuards.remove(entry.getKey());
        }
        terminated.clear();
        
        for(Receiver receiver:schedualedRemoval){
            receiversPos.remove(receiver.location, receiver);
            recGuards.get(receiver).unsetReceiver(0);
            recGuards.remove(receiver);
        }
        schedualedRemoval.clear();
    }

    @Override
    public void setSeed(long seed) {
        
    }
}