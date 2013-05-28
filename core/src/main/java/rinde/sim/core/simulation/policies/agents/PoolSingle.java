package rinde.sim.core.simulation.policies.agents;

import java.util.Map.Entry;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import rinde.sim.core.model.Agent;
import rinde.sim.core.simulation.TimeInterval;
import rinde.sim.core.simulation.policies.InteractionRules;
import rinde.sim.core.simulation.policies.agents.util.LatchNode;
import rinde.sim.core.simulation.policies.agents.util.Rules;
import rinde.sim.core.simulation.time.TimeLapseHandle;

public class PoolSingle extends AgentsPolicyAbstr{
    private final ExecutorService pool;
    
    private final Rules rules = new Rules();
    
    public PoolSingle(int cores) {
        pool = Executors.newFixedThreadPool(cores);
    }
    
    @Override
    public void doTicks(TimeInterval interval) {
        LatchNode lastNode = new LatchNode();
        
        for(Entry<Agent,TimeLapseHandle> entry:agents.entrySet()){
            final Agent agent = entry.getKey();
            final TimeLapseHandle lapse = entry.getValue();
            
            final LatchNode node = lastNode;
            lastNode = node.makeNext();
            
            pool.submit(new Runnable() {
                @Override
                public void run() {
                    rules.node.set(node);
                    
                    agent.tick(lapse);
                    
                    node.done();
                }
            });
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
