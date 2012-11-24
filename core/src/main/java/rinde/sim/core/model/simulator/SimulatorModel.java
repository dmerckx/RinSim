package rinde.sim.core.model.simulator;

import java.util.ArrayList;
import java.util.List;

import rinde.sim.core.model.Model;
import rinde.sim.core.model.User;
import rinde.sim.core.model.simulator.apis.SimulatorAPI;
import rinde.sim.core.model.simulator.supported.SimulatorUnit;
import rinde.sim.core.simulation.Simulator;
import rinde.sim.core.simulation.TimeInterval;

/**
 * @author dmerckx
 *
 */
public class SimulatorModel implements Model<SimulatorUnit>, SimulatorAPI{
    
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
    public void register(SimulatorUnit unit) {
        unit.setSimulatorAPI(this);
    }

    @Override
    public void unregister(SimulatorUnit unit) {
        
    }

    @Override
    public Class<SimulatorUnit> getSupportedType() {
        return SimulatorUnit.class;
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
