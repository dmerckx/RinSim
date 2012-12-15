package rinde.sim.core.simulation.policies;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;

import rinde.sim.PreAgentGuard;
import rinde.sim.core.model.AfterTickGuard;
import rinde.sim.core.model.Agent;
import rinde.sim.core.model.InitGuard;
import rinde.sim.core.model.TimeUser;
import rinde.sim.core.simulation.TickPolicy;
import rinde.sim.core.simulation.TimeInterval;
import rinde.sim.core.simulation.TimeLapse;
import rinde.sim.core.simulation.time.TimeLapseGroup;

public class ParallelTimeUserPolicy extends ThreadState implements TickPolicy<TimeUser>{
    
    private LinkedHashSet<InitGuard> initGuards;
    private HashMap<Agent, PreAgentGuard> preAgentGuards;
    private LinkedHashSet<Agent> agents;
    private LinkedHashSet<AfterTickGuard> afterGuards = new LinkedHashSet<AfterTickGuard>();
    
    final TimeLapseGroup group = new TimeLapseGroup();

    
    @Override
    public void register(TimeUser user){
        assert isValidUser(user);
        
        //Register a simple agent
        if(user instanceof Agent && !(user instanceof PreAgentGuard)){
            agents.add((Agent) user);
            return;
        }
        
        //Register a guard acting as time user
        if(user instanceof InitGuard){
            initGuards.add((InitGuard) user);
        }
        if(user instanceof AfterTickGuard){
            afterGuards.add((AfterTickGuard) user);
        }
        if(user instanceof PreAgentGuard){
            assert agents.contains(((PreAgentGuard) user).getAgent());
            
            preAgentGuards.put(((PreAgentGuard) user).getAgent(), (PreAgentGuard) user);
        }
    }

    @Override
    public void unregister(TimeUser user) {
        assert isValidUser(user);
        
        //Unregister a simple agent
        if(user instanceof Agent && !(user instanceof PreAgentGuard)){
            agents.remove(user);
            return;
        }
        
        //Register a guard acting as time user
        if(user instanceof InitGuard){
            initGuards.remove(user);
        }
        if(user instanceof AfterTickGuard){
            afterGuards.remove(user);
        }
        if(user instanceof PreAgentGuard){
            assert !agents.contains(((PreAgentGuard) user).getAgent());
            
            preAgentGuards.remove(((PreAgentGuard) user).getAgent());
        }
    }
    
    private boolean isValidUser(TimeUser user){
        //A time user is either a simple agent..
        boolean simpleAgent = (user instanceof Agent      
                && !(user instanceof InitGuard)
                && !(user instanceof PreAgentGuard)
                && !(user instanceof AfterTickGuard));
        
        //..or a guard acting as time user. 
        boolean guard = (user instanceof InitGuard 
                || (user instanceof PreAgentGuard)
                || (user instanceof AfterTickGuard));
        
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
        
        for(final Agent agent:agents){
            final ThreadLocal<CountDownLatch> local = previousBarrier;
            final CountDownLatch barrier = new CountDownLatch(1);
            barriers.add(barrier);
            
            final LinkedList<Agent> agentsOrdered = new LinkedList<Agent>();
            Agent last = agent;
            agentsOrdered.add(last);
            while(preAgentGuards.containsKey(last)){
                assert(preAgentGuards.get(last).getAgent() == last);
                
                last = preAgentGuards.get(last);
                agentsOrdered.addFirst(last);
            }
            
            futures.add(pool.submit(new Runnable() {
                @Override
                public void run() {
                    local.set(barrier);
                    TimeLapse lapse = group.forge(interval);
                    
                    for(Agent a:agentsOrdered){
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
        
        for(final AfterTickGuard g:afterGuards){
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
