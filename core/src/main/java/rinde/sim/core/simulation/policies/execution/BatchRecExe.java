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

public class BatchRecExe extends Execution{
    protected final int batchSize;
    protected final Rules2 rules;
    
    public BatchRecExe(int batchSize) {
        this.batchSize = batchSize;
        this.rules = new Rules2();
    }
    
    @Override
    public void execute(Iterator<Entry<Agent, TimeLapseHandle>> it) {
        LatchNode lastNode = new LatchNode();
        
        int c = 0;
        List<Entry<Agent,TimeLapseHandle>> batch = Lists.newArrayListWithCapacity(batchSize);
        
        //The main thread starts by dividing the work in pieces
        while(it.hasNext()){
            Entry<Agent, TimeLapseHandle> entry = it.next();
            batch.add(entry);
            c = (c + 1) % batchSize;
            
            if(c == 0 || !it.hasNext()){
                pool.addTask(new Task2(batch, lastNode, rules));
                
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


class Task2 implements Runnable{
    protected List<Entry<Agent, TimeLapseHandle>> batch;
    protected LatchNode node;
    protected Rules2 rules;
    
    private int counter;
    
    public Task2(List<Entry<Agent,TimeLapseHandle>> batch, LatchNode node, Rules2 rules) {
        this.batch = batch;
        this.node = node;
        this.rules = rules;
        this.counter = 0;
    }
    
    public void run(){
        rules.node.set(node);
        rules.task.set(this);
        
        startNext();
        
        node.done();
        
        batch.clear();
        batch = null;
        node = null;
        rules = null;
    }
    
    public void startNext(){
        while(counter < batch.size()){
            counter++;
            Entry<Agent, TimeLapseHandle> e = batch.get(counter-1);
            e.getKey().tick(e.getValue());
        }
    }
}


class Rules2 extends Rules {
    public final ThreadLocal<Task2> task = new ThreadLocal<Task2>();
    
    @Override
    public void awaitAllPrevious() {
        if( node.get() == null)
            return;
        
        task.get().startNext();
        try {
            node.get().awaitPrevious();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isDeterministic() {
        return true;
    }
}