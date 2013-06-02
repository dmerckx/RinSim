package rinde.sim.core.simulation.policies.execution;

import java.util.List;

import rinde.sim.core.simulation.policies.InteractionRules;
import rinde.sim.core.simulation.policies.agents.AgentContainer;
import rinde.sim.core.simulation.policies.agents.Execution;
import rinde.sim.core.simulation.policies.agents.util.LatchNode;
import rinde.sim.core.simulation.policies.agents.util.Rules;

public class BatchExe extends Execution{
    protected final int batchSize;
    protected final Rules rules;
    
    public BatchExe(int batchSize) {
        this.batchSize = batchSize;
        this.rules = new Rules();
    }
    
    @Override
    public LatchNode execute(LatchNode startNode, List<AgentContainer> containers) {
        LatchNode lastNode = startNode;
        
        int max = containers.size();
        //Divide the work and feed it to the thread pool
        for(int i = 0; i < containers.size(); i += batchSize){
            int j = Math.min(i + batchSize, max);
            pool.addTask(new Task(containers.subList(i, j), lastNode, rules));
            lastNode = lastNode.makeNext();
        }
        
        return lastNode;
    }

    @Override
    public InteractionRules getRules() {
        return rules;
    }

    @Override
    public String toString() {
        return "BatchExe";
    }
}

class Task implements Runnable{
    protected final List<AgentContainer> batch;
    protected final LatchNode node;
    protected final Rules rules;
    
    public Task(List<AgentContainer> batch, LatchNode node, Rules rules) {
        this.batch = batch;
        this.node = node;
        this.rules = rules;
    }
    
    public void run(){
        rules.node.set(node);
        
        for(AgentContainer c:batch){
            c.doTick();
        }
        
        node.done();
    }
}