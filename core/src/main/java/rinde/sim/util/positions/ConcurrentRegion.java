package rinde.sim.util.positions;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import rinde.sim.core.graph.Point;
import rinde.sim.core.model.pdp.users.DeliveryPoint;
import rinde.sim.core.model.road.users.RoadUser;
import rinde.sim.core.simulation.TimeInterval;
import rinde.sim.util.Tuple;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class ConcurrentRegion<T extends RoadUser<?>> extends Region implements Comparator<Tuple<T, Action>>{
    private final TimeInterval globalTime;
    
    public final Collection<T> values;
    private final List<Tuple<T, Action>> updates;
    
    private long lastUpdate = -100;
    
    public ConcurrentRegion(int x, int y, TimeInterval globalTime) {
        super(x, y);
        this.globalTime = globalTime;
        
        this.values = Sets.newLinkedHashSet();
        this.updates = Lists.newArrayList();
    }
    
    private void update(){
        if(globalTime.getStartTime() == lastUpdate)
            return;

        synchronized(this){
            if(globalTime.getStartTime() == lastUpdate)
                return;

            Collections.sort(updates, this);

            for(Tuple<T, Action> u:updates){
                switch (u.getValue()) {
                case ADD:
                    values.add(u.getKey());
                    break;
                case REMOVE:
                    values.remove(u.getKey());
                    break;
                }
            }
            lastUpdate = globalTime.getStartTime();
        }
    }
    
    public void directAdd(T user){
        values.add(user);
    }
    
    public void directRemove(T user){
        values.remove(user);
    }
    
    public synchronized void addUser(T user){
        update();
        updates.add(Tuple.create(user, Action.ADD));
    }
    
    public synchronized void removeUser(T user){
        update();
        updates.add(Tuple.create(user, Action.REMOVE));
    }
    
    public void queryUpon(Point point, double radius, Query query){
        update();
        
        for(T user:values){
            if(Point.distance(point, user.getRoadState().getLocation()) < radius){
                //if(!query.getType().isInstance(user)) continue;
                query.process(user);
            }
        }
    }

    @Override
    public int compare(Tuple<T, Action> o1, Tuple<T, Action> o2) {
        return o1.getKey().getRoadState().getId().compareTo(
                o2.getKey().getRoadState().getId());
    }

}

enum Action{
    ADD,
    REMOVE
}
