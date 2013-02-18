package rinde.sim.core.simulation.policies.parallel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import rinde.sim.core.model.Agent;
import rinde.sim.core.model.InitUser;
import rinde.sim.core.simulation.TimeInterval;
import rinde.sim.core.simulation.time.TimeLapseHandle;

/**
 * Parallel time user policy which threads the tick operation of every single agent
 * as an individuel task.
 * 
 * @author dmerckx
 */
public class PSingleTimeUserPolicy extends PTimeUserPolicy{

    @Override
    public void performTicks(TimeInterval interval) {
        for(InitUser user:initUsers){
            user.init();
        }
        initUsers.clear();
        
        List<Future<?>> futures = new ArrayList<Future<?>>();
        List<CountDownLatch> barriers = new ArrayList<CountDownLatch>();
        
        updateLapses();
        
        for(Entry<Agent,TimeLapseHandle> entry:agents.entrySet()){
            final Agent agent = entry.getKey();
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
}
