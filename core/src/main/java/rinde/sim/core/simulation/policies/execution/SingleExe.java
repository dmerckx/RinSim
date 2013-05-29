package rinde.sim.core.simulation.policies.execution;

import java.util.Iterator;
import java.util.Map.Entry;

import rinde.sim.core.model.Agent;
import rinde.sim.core.simulation.policies.InteractionRules;
import rinde.sim.core.simulation.policies.agents.Execution;
import rinde.sim.core.simulation.policies.agents.util.LatchNode;
import rinde.sim.core.simulation.policies.agents.util.Rules;
import rinde.sim.core.simulation.time.TimeLapseHandle;

public class SingleExe  extends Execution{
    protected final Rules rules;
    
    public SingleExe() {
        this.rules = new Rules();
    }
    
    @Override
    public void execute(Iterator<Entry<Agent, TimeLapseHandle>> it) {
        LatchNode lastNode = new LatchNode();
        
        //The main thread start by dividing the work in pieces
        while(it.hasNext()){
            Entry<Agent, TimeLapseHandle> entry = it.next();
            pool.addTask(new RealSingleTask(entry.getKey(), entry.getValue(), lastNode, rules));
            lastNode = lastNode.makeNext();
        }
    }

    @Override
    public InteractionRules getRules() {
        return rules;
    }

}

class RealSingleTask implements Runnable{
    protected final Agent agent;
    protected final TimeLapseHandle handle;
    protected final LatchNode node;
    protected final Rules rules;
    
    public RealSingleTask(Agent agent, TimeLapseHandle handle, LatchNode node, Rules rules) {
        this.agent = agent;
        this.handle = handle;
        this.node = node;
        this.rules = rules;
    }
    
    public void run(){
        rules.node.set(node);
        
        agent.tick(handle);
        
        node.done();
    }
}