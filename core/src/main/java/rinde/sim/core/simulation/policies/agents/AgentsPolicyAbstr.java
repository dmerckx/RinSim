package rinde.sim.core.simulation.policies.agents;

import java.util.HashMap;
import java.util.List;

import rinde.sim.core.model.Agent;
import rinde.sim.core.model.InitUser;
import rinde.sim.core.simulation.TimeInterval;
import rinde.sim.core.simulation.policies.AgentsPolicy;
import rinde.sim.core.simulation.time.TimeLapseHandle;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public abstract class AgentsPolicyAbstr implements AgentsPolicy{
    
    public static final int NR_CORES = 4;

    protected HashMap<Agent, TimeLapseHandle> agents = Maps.newLinkedHashMap();
    protected List<InitUser> initUsers = Lists.newArrayList();

    @Override
    public void register(Agent agent, TimeLapseHandle handle){
        assert agent != null;
        assert handle != null;
        
        assert !agents.containsKey(agent);
        
        agents.put(agent, handle);
    }

    @Override
    public void unregister(Agent agent) {
        assert agents.containsKey(agent);
        
        agents.remove(agent);
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
    public boolean canRegisterDuringExecution() {
        return false;
    }

    @Override
    public boolean canUnregisterDuringExecution() {
        return false;
    }

}