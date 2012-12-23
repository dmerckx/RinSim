package rinde.sim.core.simulation.policies;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import rinde.sim.TickGuard;
import rinde.sim.core.model.Agent;
import rinde.sim.core.model.InitGuard;
import rinde.sim.core.model.TimeUser;
import rinde.sim.core.model.User;
import rinde.sim.core.simulation.TimeInterval;
import rinde.sim.core.simulation.TimeLapse;
import rinde.sim.core.simulation.time.TimeLapseGroup;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Sets;

public class ParallelTimeUserPolicy extends ParallelExecution implements TimeUserPolicy{
    
    private LinkedHashSet<InitGuard> initGuards = Sets.newLinkedHashSet();
    private ListMultimap<User<?>, Agent> agents = ArrayListMultimap.create();
    
    final TimeLapseGroup group = new TimeLapseGroup();

    
    public void register(User<?> originalUser, List<TimeUser> relatedTimeUsers){
        System.out.println(" -- register");
        
        for(TimeUser user:relatedTimeUsers){
            assert isValidUser(user);

            if(user instanceof InitGuard){
                initGuards.add((InitGuard) user);
            }
            
            if(!(user instanceof Agent))
                continue;
            
            agents.put(originalUser, (Agent) user);
        }
    }

    public void unregister(User<?> originalUser) {
        assert agents.containsKey(originalUser);
        
        agents.removeAll(originalUser);
    }
    
    private boolean isValidUser(TimeUser user){
        //A time user is either a simple agent..
        boolean simpleAgent = (user instanceof Agent      
                && !(user instanceof InitGuard)
                && !(user instanceof TickGuard));
        
        //..or a guard acting as time user. 
        boolean guard = (user instanceof InitGuard 
                || (user instanceof TickGuard));
        
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
        
        for(final User<?> key:agents.keySet()){
            final List<Agent> toExecute= agents.get(key); //TODO: check if execution order is not random !!
            final ThreadLocal<CountDownLatch> local = previousBarrier;
            final CountDownLatch barrier = new CountDownLatch(1);
            barriers.add(barrier);
            
            Future<?> f = pool.submit(new Runnable() {
                @Override
                public void run() {
                    local.set(barrier);
                    TimeLapse lapse = group.forge(interval);
                    
                    for(Agent a:toExecute){
                        a.tick(lapse);
                    }
                    
                    local.set(null);
                }
            });
            
            futures.add(f);
        }
        
        for(int i = 0; i < futures.size(); i++){
            try {
                futures.get(i).get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
            
            barriers.get(i).countDown();
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
