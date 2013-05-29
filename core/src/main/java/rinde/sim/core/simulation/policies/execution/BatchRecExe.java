package rinde.sim.core.simulation.policies.execution;

import java.util.List;

import rinde.sim.core.simulation.policies.InteractionRules;
import rinde.sim.core.simulation.policies.agents.AgentContainer;
import rinde.sim.core.simulation.policies.agents.Execution;
import rinde.sim.core.simulation.policies.agents.util.LatchNode;
import rinde.sim.core.simulation.policies.agents.util.Rules;

public class BatchRecExe extends Execution{
    protected final int batchSize;
    protected final Rules2 rules;
    
    public BatchRecExe(int batchSize) {
        this.batchSize = batchSize;
        this.rules = new Rules2();
    }
    
    @Override
    public void execute(List<AgentContainer> containers) {
        LatchNode lastNode = new LatchNode();
        
        int max = containers.size();
        //Divide the work and feed it to the thread pool
        for(int i = 0; i < containers.size(); i += batchSize){
            int j = Math.min(i + batchSize, max);
            pool.addTask(new Task2(containers.subList(i, j), lastNode, rules));
            lastNode = lastNode.makeNext();
        }
    }

    @Override
    public InteractionRules getRules() {
        return rules;
    }

}


class Task2 implements Runnable{
    protected List<AgentContainer> batch;
    protected LatchNode node;
    protected Rules2 rules;
    
    private int counter;
    
    public Task2(List<AgentContainer> batch, LatchNode node, Rules2 rules) {
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
        
        batch = null;
        node = null;
        rules = null;
    }
    
    public void startNext(){
        while(counter < batch.size()){
            counter++;
            batch.get(counter-1).doTick();
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