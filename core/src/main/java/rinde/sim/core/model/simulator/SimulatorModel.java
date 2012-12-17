package rinde.sim.core.model.simulator;

import java.util.ArrayList;
import java.util.List;

import rinde.sim.core.model.Data;
import rinde.sim.core.model.Model;
import rinde.sim.core.model.User;
import rinde.sim.core.model.simulator.apis.SimulatorAPI;
import rinde.sim.core.model.simulator.users.SimulatorUser;
import rinde.sim.core.simulation.Simulator;
import rinde.sim.core.simulation.TimeInterval;
import rinde.sim.core.simulation.UserInit;

import com.google.common.collect.Lists;

/**
 * @author dmerckx
 *
 */
public class SimulatorModel implements Model<Data, SimulatorUser<?>>, SimulatorAPI{
    
    public final List<UserInit<?>> objectsToAdd;
    public final List<User<?>> objectsToRemove;
    private Simulator sim;
    
    public SimulatorModel(Simulator sim) {
        objectsToAdd = Lists.newArrayList();
        objectsToRemove = Lists.newArrayList();
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
    public List<UserInit<?>> register(SimulatorUser<?> user, Data data) {
        assert user!=null : "User can not be null.";
        
        user.setSimulatorAPI(this);
        
        return Lists.newArrayList();
    }

    @Override
    public List<User<?>> unregister(SimulatorUser<?> user) {
        return Lists.newArrayList();
    }

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
