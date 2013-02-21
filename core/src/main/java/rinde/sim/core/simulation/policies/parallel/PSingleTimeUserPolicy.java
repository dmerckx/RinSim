package rinde.sim.core.simulation.policies.parallel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import rinde.sim.core.model.Agent;
import rinde.sim.core.simulation.TimeInterval;
import rinde.sim.core.simulation.policies.InteractionRules;
import rinde.sim.core.simulation.time.TimeLapseHandle;

/**
 * Parallel time user policy which threads the tick operation of every single agent
 * as an individuel task.
 * 
 * @author dmerckx
 */
public class PSingleTimeUserPolicy extends PTimeUserPolicy{

    private final ExecutorService pool = Executors.newFixedThreadPool(NR_CORES);
    
    private final Rules rules = new Rules();

    public PSingleTimeUserPolicy() {}
    
    @Override
    public void doTicks(TimeInterval interval) {
        List<Future<?>> futures = new ArrayList<Future<?>>();
        List<CountDownLatch> barriers = new ArrayList<CountDownLatch>();
        
        for(Entry<Agent,TimeLapseHandle> entry:agents.entrySet()){
            final Agent agent = entry.getKey();
            final TimeLapseHandle lapse = entry.getValue();
            
            final CountDownLatch barrier = new CountDownLatch(1);
            barriers.add(barrier);
            
            Future<?> f = pool.submit(new Runnable() {
                @Override
                public void run() {
                    rules.previousLatch.set(barrier);
                    
                    agent.tick(lapse);
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
    public InteractionRules getInteractionRules() {
        return rules;
    }

    @Override
    public void warmUp() {
        
    }

    @Override
    public void shutDown() {
        pool.shutdown();
    }
}
