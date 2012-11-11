package rinde.sim.core.model.simulator;

import rinde.sim.core.model.Model;
import rinde.sim.core.model.SimulatorModelAPI;
import rinde.sim.core.simulation.TimeInterval;
import rinde.sim.core.simulation.types.PrimaryTickListener;

/**
 * @author dmerckx
 *
 */
public class SimulatorModel implements Model<SimulatorUser>, PrimaryTickListener{
    
    private SimulatorHelper helper;
    private SimulatorModelAPI api;
    
    public SimulatorModel() {
        this.helper = new SimulatorHelper();
    }

    @Override
    public void setSimulatorAPI(SimulatorModelAPI api) {
        this.api = api;
    }
    
    @Override
    public void register(SimulatorUser element) {
        element.setSimulator(helper);
    }

    @Override
    public void unregister(SimulatorUser element) {}

    @Override
    public Class<SimulatorUser> getSupportedType() {
        return SimulatorUser.class;
    }

    @Override
    public void tick(TimeInterval t) {
        for(Object o: helper.objectsToAdd){
            api.register(o);
        }
        helper.objectsToAdd.clear();
        for(Object o: helper.objectsToRemove){
            api.unregister(o);
        }
        helper.objectsToRemove.clear();
    }

}
