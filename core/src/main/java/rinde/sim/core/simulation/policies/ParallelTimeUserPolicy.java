package rinde.sim.core.simulation.policies;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import rinde.sim.core.model.Agent;
import rinde.sim.core.model.InitUser;
import rinde.sim.core.model.User;
import rinde.sim.core.simulation.TimeInterval;
import rinde.sim.core.simulation.time.TimeLapseHandle;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class ParallelTimeUserPolicy extends ParallelExecution implements TimeUserPolicy{
    
    private HashMap<User<?>, TimeLapseHandle> agents = Maps.newLinkedHashMap();
    private List<InitUser> initUsers = Lists.newArrayList();
    
    public void register(User<?> agent, TimeLapseHandle handle){
        assert agent != null;
        assert handle != null;
        assert !agents.containsKey(handle);
        
        agents.put(agent, handle);
    }

    public void unregister(User<?> agent) {
        assert agents.containsKey(agent);
        
        agents.remove(agent);
    }

    @Override
    public void addInituser(InitUser user) {
        initUsers.add(user);
    }

    @Override
    public void performTicks(TimeInterval interval) {
        for(InitUser user:initUsers){
            user.init();
        }
        initUsers.clear();
        
        List<Future<?>> futures = new ArrayList<Future<?>>();
        List<CountDownLatch> barriers = new ArrayList<CountDownLatch>();
        
        for(Entry<User<?>,TimeLapseHandle> entry:agents.entrySet()){
            entry.getValue().nextStep();
        }
            
        for(Entry<User<?>,TimeLapseHandle> entry:agents.entrySet()){
            if(!(entry.getKey() instanceof Agent))
                continue;
                
            final Agent agent = (Agent) entry.getKey();
            final TimeLapseHandle lapse = entry.getValue();
            
            final CountDownLatch barrier = new CountDownLatch(1);
            barriers.add(barrier);
            
            Future<?> f = pool.submit(new Runnable() {
                @Override
                public void run() {
                    previousBarrier.set(barrier);
                    
                    agent.tick(lapse);
                    
                    previousBarrier.set(null);
                }
            });
            
            futures.add(f);
        }
        
        for(int i = 0; i < futures.size(); i++){
            barriers.get(i).countDown();
            try {
                futures.get(i).get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
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
