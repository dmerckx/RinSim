package rinde.sim.core.model.simulator;

import java.util.List;

import rinde.sim.core.model.Data;
import rinde.sim.core.model.Model;
import rinde.sim.core.model.User;
import rinde.sim.core.model.simulator.apis.SimulatorAPI;
import rinde.sim.core.model.simulator.users.SimulatorUser;
import rinde.sim.core.simulation.Simulator;
import rinde.sim.core.simulation.TimeInterval;
import rinde.sim.core.simulation.UserInit;
import rinde.sim.core.simulation.time.TimeLapseHandle;

import com.google.common.collect.Lists;

/**
 * A model allowing its users to register and unregister other agents
 * to the simulator.
 * 
 * This model supports the following types:
 *  - {@link SimulatorUser} : {@link Data}
 *  
 * @author dmerckx
 */
public class SimulatorModel implements Model<Data, SimulatorUser<?>>, SimulatorAPI{
    
    private final List<UserInit<?>> objectsToAdd = Lists.newArrayList();
    private final List<User<?>> objectsToRemove = Lists.newArrayList();
    private Simulator sim;
    
    /**
     * Create a new simulator model.
     * @param sim The actual simulator.
     */
    @SuppressWarnings("hiding")
    public SimulatorModel(Simulator sim) {
        this.sim = sim;
    }

    
    // ----- SIMULATOR API ----- //
    
    @Override
    public <D extends Data> void registerUser(User<D> user, D data) {
        objectsToAdd.add(UserInit.create(user, data));
    }

    @Override
    public void unregisterUser(User<?> o) {
        objectsToRemove.add(o);
    }
    
    
    // ----- MODEL ----- //

    @Override
    public void setSeed(long seed) {
        
    }
    
    @Override
    public List<UserInit<?>> register(SimulatorUser<?> user, Data data, TimeLapseHandle handle) {
        assert user!=null : "User can not be null.";
        
        user.setSimulatorAPI(this);
        
        return Lists.newArrayList();
    }

    @Override
    public List<User<?>> unregister(SimulatorUser<?> user) {
        return Lists.newArrayList();
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public Class<SimulatorUser<?>> getSupportedType() {
        return (Class) SimulatorUser.class;
    }

    @Override
    public void tick(TimeInterval t) {
        for(UserInit<?> i: objectsToAdd){
            doRegister(i);
        }
        objectsToAdd.clear();
        for(User<?> o: objectsToRemove){
            sim.unregisterUser(o);
        }
        objectsToRemove.clear();
    }
    
    private <D extends Data> void doRegister(UserInit<D> init) {
        sim.registerUser(init.user, init.data);
    }

}
