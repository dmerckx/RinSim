package rinde.sim.core.simulation.policies.execution;

import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import rinde.sim.core.model.Agent;
import rinde.sim.core.simulation.policies.InteractionRules;
import rinde.sim.core.simulation.policies.agents.Execution;
import rinde.sim.core.simulation.policies.agents.util.LatchNode;
import rinde.sim.core.simulation.policies.agents.util.Rules;
import rinde.sim.core.simulation.time.TimeLapseHandle;

import com.google.common.collect.Lists;

public class BatchExe extends Execution{
    protected final int batchSize;
    protected final Rules rules;
    
    public BatchExe(int batchSize) {
        this.batchSize = batchSize;
        this.rules = new Rules();
    }
    
    @Override
    public void execute(Iterator<Entry<Agent, TimeLapseHandle>> it) {
        LatchNode lastNode = new LatchNode();
        
        int c = 0;
        List<Entry<Agent,TimeLapseHandle>> batch = Lists.newArrayListWithCapacity(batchSize);
        
        //Divide the work and feed it to the thread pool
        while(it.hasNext()){
            Entry<Agent, TimeLapseHandle> entry = it.next();
            batch.add(entry);
            c = (c + 1) % batchSize;
            
            if(c == 0 || !it.hasNext()){
                pool.addTask(new Task(batch, lastNode, rules));
                
                lastNode = lastNode.makeNext();
                batch = Lists.newArrayList();
            }
        }
    }

    @Override
    public InteractionRules getRules() {
        return rules;
    }

}

class Task implements Runnable{
    protected final List<Entry<Agent, TimeLapseHandle>> batch;
    protected final LatchNode node;
    protected final Rules rules;
    
    public Task(List<Entry<Agent,TimeLapseHandle>> batch, LatchNode node, Rules rules) {
        this.batch = batch;
        this.node = node;
        this.rules = rules;
    }
    
    public void run(){
        rules.node.set(node);
        
        for(Entry<Agent, TimeLapseHandle> e:batch){
            e.getKey().tick(e.getValue());
        }
        
        node.done();
    }
}