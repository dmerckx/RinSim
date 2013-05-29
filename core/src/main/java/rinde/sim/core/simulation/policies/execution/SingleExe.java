package rinde.sim.core.simulation.policies.execution;

import java.util.List;

import rinde.sim.core.simulation.policies.InteractionRules;
import rinde.sim.core.simulation.policies.agents.AgentContainer;
import rinde.sim.core.simulation.policies.agents.Execution;
import rinde.sim.core.simulation.policies.agents.util.LatchNode;
import rinde.sim.core.simulation.policies.agents.util.Rules;

public class SingleExe  extends Execution{
    protected final Rules rules;
    
    public SingleExe() {
        this.rules = new Rules();
    }
    
    @Override
    public void execute(List<AgentContainer> agents) {
        LatchNode lastNode = new LatchNode();
        
        //The main thread start by dividing the work in pieces
        for(AgentContainer c:agents){
            pool.addTask(new RealSingleTask(c, lastNode, rules));
            lastNode = lastNode.makeNext();
        }
    }

    @Override
    public InteractionRules getRules() {
        return rules;
    }

}

class RealSingleTask implements Runnable{
    protected final AgentContainer container;
    protected final LatchNode node;
    protected final Rules rules;
    
    public RealSingleTask(AgentContainer c, LatchNode node, Rules rules) {
        this.container = c;
        this.node = node;
        this.rules = rules;
    }
    
    public void run(){
        rules.node.set(node);
        
        container.doTick();
        
        node.done();
    }
}