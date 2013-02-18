package rinde.sim.core.simulation.policies.parallel;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import rinde.sim.core.model.Agent;
import rinde.sim.core.model.InitUser;
import rinde.sim.core.simulation.TimeInterval;
import rinde.sim.core.simulation.time.TimeLapseHandle;

import com.google.common.collect.Lists;

/**
 * Parallel time user policy which takes a batch of tick operations from multiple different
 * agents and threads that as an individuel task.
 * 
 * @author dmerckx
 */
public class PBatchTimeUserPolicy extends PTimeUserPolicy{
    
    private int batchSize;
    
    public PBatchTimeUserPolicy(int batchSize) {
        this.batchSize = batchSize;
    }

    @Override
    public void performTicks(TimeInterval interval) {
        for(InitUser user:initUsers){
            user.init();
        }
        initUsers.clear();
        
        List<Future<?>> futures = new ArrayList<Future<?>>();
        List<CountDownLatch> barriers = new ArrayList<CountDownLatch>();
        
        updateLapses();
        
        Iterator<Entry<Agent, TimeLapseHandle>> it = agents.entrySet().iterator();
        
        int c = 0;
        List<Entry<Agent,TimeLapseHandle>> batch = Lists.newArrayList();
        while(it.hasNext()){
            Entry<Agent, TimeLapseHandle> entry = it.next();
            batch.add(entry);
            c = (c + 1) % batchSize;
            
            if(c == 0 || !it.hasNext()){
                final CountDownLatch barrier = new CountDownLatch(1);
                barriers.add(barrier);
                final List<Entry<Agent,TimeLapseHandle>> b = batch;
                
                Future<?> f = pool.submit(new Runnable() {
                    @Override
                    public void run() {
                        //previousBarrier.set(barrier);
                        
                        for(Entry<Agent, TimeLapseHandle> e:b){
                            e.getKey().tick(e.getValue());
                        }
                    }
                });
                futures.add(f);
                batch = Lists.newArrayList();
            }
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
