package rinde.sim.core.simulation.policies.agents;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import rinde.sim.core.model.Agent;
import rinde.sim.core.model.InitUser;
import rinde.sim.core.simulation.TimeInterval;
import rinde.sim.core.simulation.policies.AgentsPolicy;
import rinde.sim.core.simulation.time.TimeLapseHandle;
import rinde.sim.util.Rectangle;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public abstract class AgentsPolicyAbstr implements AgentsPolicy{
    protected Map<Agent, Integer> agentsMapping = Maps.newHashMap();
    protected List<AgentContainer> agents = Lists.newArrayList();
    protected LinkedList<Integer> nulls = Lists.newLinkedList();
    
    protected List<InitUser> initUsers = Lists.newArrayList();

    @Override
    public synchronized void register(Agent agent, TimeLapseHandle handle){
        assert agent != null;
        assert handle != null;
        assert !agentsMapping.containsKey(agent);
        
        if(nulls.isEmpty()){
            agents.add(new AgentContainer(agent, handle));
            agentsMapping.put(agent, agents.size()-1);
        }
        else{
            Integer index = nulls.pop();
            agents.set(index, new AgentContainer(agent, handle));
            agentsMapping.put(agent, index);
        }
    }

    @Override
    public synchronized void unregister(Agent agent) {
        assert agentsMapping.containsKey(agent);
        
        int index = agentsMapping.get(agent);
        agents.set(index, NullAgent.instance);
        nulls.add(index);
        agentsMapping.remove(agent);
    }

    @Override
    public void addInituser(InitUser user) {
        initUsers.add(user);
    }

    @Override
    public final void performTicks(TimeInterval interval){
        for(InitUser user:initUsers){
            user.init();
        }
        initUsers.clear();
        
        doTicks(interval);
    }
    
    protected abstract void doTicks(TimeInterval interval);
    
    @Override
    public void init(Rectangle mapSize) {
        agents.clear();
        initUsers.clear();
    }
    
    @Override
    public boolean canRegisterDuringExecution() {
        return false;
    }

    @Override
    public boolean canUnregisterDuringExecution() {
        return false;
    }
    
    private static class NullAgent extends AgentContainer{
        public static final NullAgent instance = new NullAgent();
        
        private NullAgent() {
            super(null, null);
        }
        
        @Override
        public void doTick() {}
    }
}