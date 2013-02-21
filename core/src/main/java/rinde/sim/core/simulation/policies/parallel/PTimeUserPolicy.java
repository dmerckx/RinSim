package rinde.sim.core.simulation.policies.parallel;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import rinde.sim.core.Monitor;
import rinde.sim.core.model.Agent;
import rinde.sim.core.model.InitUser;
import rinde.sim.core.model.User;
import rinde.sim.core.simulation.TimeInterval;
import rinde.sim.core.simulation.policies.InteractionRules;
import rinde.sim.core.simulation.policies.TimeUserPolicy;
import rinde.sim.core.simulation.time.TimeLapseHandle;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public abstract class PTimeUserPolicy implements TimeUserPolicy{
    
    public static final int NR_CORES = 4;

    //TODO lapses are not removed
    protected List<TimeLapseHandle> lapses = Lists.newArrayList();
    protected HashMap<Agent, TimeLapseHandle> agents = Maps.newLinkedHashMap();
    protected List<InitUser> initUsers = Lists.newArrayList();
    
    public void register(User<?> user, TimeLapseHandle handle){
        assert user != null;
        assert handle != null;
        
        lapses.add(handle);
        if(user instanceof Agent){
            assert !agents.containsKey(user);
            agents.put((Agent) user, handle);
        }
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
    public final void performTicks(TimeInterval interval){
        for(InitUser user:initUsers){
            user.init();
        }
        initUsers.clear();
        
        Monitor.get().startLapses();
        updateLapsesSerial();
        Monitor.get().endLapses();
        
        doTicks(interval);
    }
    
    protected abstract void doTicks(TimeInterval interval);
    
    public void updateLapsesSerial(){
        for(TimeLapseHandle lapse:lapses){
            lapse.nextStep();
        }
    }
    
    /*public void updateLapsesParallel(){
        int nrBatches = NR_CORES * 4;
        
        final CountDownLatch latch = new CountDownLatch(nrBatches);
        
        final List<TimeLapseHandle> lapsesF = lapses;
        int size = lapses.size();
        int step = (int) Math.ceil(size / 4);
        int start = size;
        
        for(int i = 0; i < nrBatches; i++){
            final int from = Math.max(0, start - step);
            final int to = start;
            
            pool.submit(new Runnable() {
                @Override
                public void run() {
                    for(int j = from; j < to; j++){
                        lapses.get(j).nextStep();
                    }
                    latch.countDown();
                }
            });
            
            start -= step;
            
            //System.out.println("from: " + from + " to: " + to);
        }
        
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }*/

    @Override
    public boolean canRegisterDuringExecution() {
        return false;
    }

    @Override
    public boolean canUnregisterDuringExecution() {
        return false;
    }

}

class Rules implements InteractionRules {
    public final ThreadLocal<CountDownLatch> previousLatch = new ThreadLocal<CountDownLatch>();
    
    @Override
    public void awaitAllPrevious() {
        if( previousLatch.get() == null)
            return;

        try {
            previousLatch.get().await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isDeterministic() {
        return true;
    }
}
