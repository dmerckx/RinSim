package rinde.sim.core.model.simulator;

import java.util.ArrayList;
import java.util.List;

class SimulatorHelper implements SimulatorAPI{

    public final List<Object> objectsToAdd;
    public final List<Object> objectsToRemove;

    public SimulatorHelper() {
        objectsToAdd = new ArrayList<Object>();
        objectsToRemove = new ArrayList<Object>();
    }
    
    @Override
    public void register(Object o) {
        objectsToAdd.add(o);
    }

    @Override
    public void unregister(Object o) {
        objectsToRemove.add(o);
    }

}
