package rinde.sim.core.model.simulator;

import java.util.ArrayList;
import java.util.List;

import rinde.sim.core.model.Data;
import rinde.sim.core.model.Model;
import rinde.sim.core.model.User;
import rinde.sim.core.model.simulator.apis.SimulatorAPI;
import rinde.sim.core.model.simulator.users.SimulatorUser;
import rinde.sim.core.simulation.Simulator;
import rinde.sim.core.simulation.SimulatorToModelAPI;
import rinde.sim.core.simulation.TimeInterval;

/**
 * @author dmerckx
 *
 */
public class SimulatorModel implements Model<Data, SimulatorUser<?>>, SimulatorAPI{
    
    public final List<Object> objectsToAdd;
    public final List<Object> objectsToRemove;
    private Simulator sim;
    
    public SimulatorModel(Simulator sim) {
        objectsToAdd = new ArrayList<Object>();
        objectsToRemove = new ArrayList<Object>();
    }

    
    // ----- SIMULATOR API ----- //
    
    @Override
    public void registerUser(User o) {
        objectsToAdd.add(o);
    }

    @Override
    public void unregisterUser(User o) {
        objectsToRemove.add(o);
    }
    
    
    // ----- MODEL ----- //
    
    @Override
    public void register(SimulatorToModelAPI sim2, SimulatorUser<?> user, Data data) {
        assert sim!=null: "Sim can not be null.";
        assert user!=null : "User can not be null.";
        
        user.setSimulatorAPI(this);
    }

    @Override
    public void unregister(SimulatorUser<?> user) {
        
    }

    @Override
    public Class<SimulatorUser<?>> getSupportedType() {
        return (Class) SimulatorUser.class;
    }

    @Override
    public void tick(TimeInterval t) {
        for(Object o: objectsToAdd){
            sim.register(o);
        }
        objectsToAdd.clear();
        for(Object o: objectsToRemove){
            sim.unregister(o);
        }
        objectsToRemove.clear();
    }

}
