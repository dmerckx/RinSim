package rinde.sim.core.simulation.policies.agents;

import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import rinde.sim.core.model.Agent;
import rinde.sim.core.simulation.TimeInterval;
import rinde.sim.core.simulation.policies.InteractionRules;
import rinde.sim.core.simulation.policies.agents.util.LatchNode;
import rinde.sim.core.simulation.policies.agents.util.Rules;
import rinde.sim.core.simulation.time.TimeLapseHandle;

import com.google.common.collect.Lists;

/**
 * Parallel time user policy which takes a batch of tick operations from multiple different
 * agents and threads that as an individuel task.
 * 
 * @author dmerckx
 */
public class ContPoolBatch extends AgentsPolicyAbstr{

    private final ExecutorService pool;
    private int batchSize;
    
    private final Rules rules = new Rules();
    
    public ContPoolBatch(int batchSize, int nrThreads) {
        this.batchSize = batchSize;
        pool = Executors.newFixedThreadPool(nrThreads);
    }

    @Override
    public void doTicks(TimeInterval interval) {
        LatchNode lastNode = new LatchNode();
        Iterator<Entry<Agent, TimeLapseHandle>> it = agents.entrySet().iterator();
        
        int c = 0;
        List<Entry<Agent,TimeLapseHandle>> batch = Lists.newArrayList();
        
        while(it.hasNext()){
            Entry<Agent, TimeLapseHandle> entry = it.next();
            batch.add(entry);
            c = (c + 1) % batchSize;
            
            if(c == 0 || !it.hasNext()){
                final LatchNode node = lastNode;
                lastNode = node.makeNext();
                
                final List<Entry<Agent,TimeLapseHandle>> b = batch;
                
                Future<?> f = pool.submit(new Runnable() {
                    @Override
                    public void run() {
                        rules.node.set(node);
                        
                        for(Entry<Agent, TimeLapseHandle> e:b){
                            e.getKey().tick(e.getValue());
                        }
                        
                        node.done();
                    }
                });
                batch = Lists.newArrayList();
            }
        }
        
        try {
            lastNode.done();
            lastNode.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public InteractionRules getInteractionRules() {
        return rules;
    }

    @Override
    public void warmUp() {
        int nrDummyTasks = 4000;
        final CountDownLatch latch = new CountDownLatch(nrDummyTasks);
        for(int i = 0; i < nrDummyTasks; i++){
            pool.submit(new Runnable() {
                @Override
                public void run() {
                    String warmup = "";
                    for(int i = 0; i < 500; i++){
                        warmup += i + "j*";
                    }
                    latch.countDown();
                }
            });
        }
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void shutDown() {
        pool.shutdown();
    }
}