package rinde.sim.core.simulation.policies;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;

import rinde.sim.FullGuard;
import rinde.sim.core.model.Agent;
import rinde.sim.core.model.InitGuard;
import rinde.sim.core.model.TimeUser;
import rinde.sim.core.model.User;
import rinde.sim.core.simulation.TimeInterval;
import rinde.sim.core.simulation.TimeLapse;
import rinde.sim.core.simulation.time.TimeLapseGroup;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

public class ParallelTimeUserPolicy extends ThreadState implements TimeUserPolicy{
    
    private LinkedHashSet<InitGuard> initGuards = Sets.newLinkedHashSet();
    private LinkedHashSet<FullGuard> fullGuards = Sets.newLinkedHashSet();
    private HashMultimap<User<?>, Agent> agents = HashMultimap.create();
    
    final TimeLapseGroup group = new TimeLapseGroup();

    
    public void register(User<?> originalUser, List<TimeUser> relatedTimeUsers){
        for(TimeUser user:relatedTimeUsers){
            assert isValidUser(user);

            if(user instanceof InitGuard){
                initGuards.add((InitGuard) user);
            }
            
            if(user instanceof FullGuard){
                fullGuards.add((FullGuard) user);
            }
            
            if(!(user instanceof Agent))
                continue;
            
            agents.put(originalUser, (Agent) user);
        }
    }

    public void unregister(User<?> originalUser) {
        assert agents.containsKey(originalUser);
        
        for(Agent agent:agents.get(originalUser)){
            if(agent instanceof FullGuard){
                fullGuards.remove(agent);
            }
        }
        
        agents.removeAll(originalUser);
    }
    
    private boolean isValidUser(TimeUser user){
        //A time user is either a simple agent..
        boolean simpleAgent = (user instanceof Agent      
                && !(user instanceof InitGuard)
                && !(user instanceof FullGuard));
        
        //..or a guard acting as time user. 
        boolean guard = (user instanceof InitGuard 
                || (user instanceof FullGuard));
        
        return simpleAgent ^ guard;
    }

    @Override
    public void performTicks(final TimeInterval interval) {
        for(InitGuard guard:initGuards){
            guard.init();
        }
        initGuards.clear();
        
        List<Future<?>> futures = new ArrayList<Future<?>>();
        List<CountDownLatch> barriers = new ArrayList<CountDownLatch>();
        
        for(final User<?> key:agents.keys()){
            final Set<Agent> toExecute= agents.get(key); //TODO: check if execution order is not random !!
            final ThreadLocal<CountDownLatch> local = previousBarrier;
            final CountDownLatch barrier = new CountDownLatch(1);
            barriers.add(barrier);
            
            futures.add(pool.submit(new Runnable() {
                @Override
                public void run() {
                    local.set(barrier);
                    TimeLapse lapse = group.forge(interval);
                    
                    for(Agent a:toExecute){
                        a.tick(lapse);
                    }
                    
                    local.set(null);
                }
            }));
        }
        
        for(int i = 0; i < futures.size(); i++){
            try {
                futures.get(i).get();
            } catch (Exception e) {
                e.printStackTrace();
            }
            barriers.get(i).countDown();
        }
        
        for(final FullGuard g:fullGuards){
            futures.add(pool.submit(new Runnable() {
                @Override
                public void run() {
                    g.afterTick(interval);
                }
            }));
        }
    }

    @Override
    public boolean canRegisterDuringExecution() {
        return false;
    }

    @Override
    public boolean canUnregisterDuringExecution() {
        return false;
    }

}
