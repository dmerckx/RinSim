package rinde.sim.core.model.pdp.supported;

import rinde.sim.core.model.interaction.guards.InteractiveGuard;
import rinde.sim.core.model.pdp.Parcel;
import rinde.sim.core.model.pdp.guards.ContainerGuard;
import rinde.sim.core.model.pdp.users.Truck;
import rinde.sim.core.model.road.guards.MovingRoadGuard;
import rinde.sim.core.model.road.guards.RoadGuard;
import rinde.sim.core.model.road.supported.MovingRoadUnit;
import rinde.sim.core.simulation.TimeInterval;
import rinde.sim.core.simulation.TimeLapse;

public class TruckUnit<P extends Parcel> implements ContainerUnit<P>, MovingRoadUnit{

    private MovingRoadGuard roadGuard;
    private InteractiveGuard interactiveGuard;
    private ContainerGuard<P> containerGuard;

    private Truck<P> truck;
    
    public TruckUnit(Truck<P> truck) {
        this.truck = truck;
    }
    
    @Override
    public void tick(TimeLapse lapse) {
        containerGuard.tick(lapse);
    }

    @Override
    public void afterTick(TimeInterval time) {
        roadGuard.afterTick(time);
        interactiveGuard.afterTick(time);
    }
    
    @Override
    public MovingRoadGuard getRoadAPI() {
        return roadGuard;
    }

    @Override
    public void setRoadGuard(RoadGuard guard) {
        this.roadGuard = (MovingRoadGuard) guard;
    }

    @Override
    public InteractiveGuard getInteractiveAPI() {
        return interactiveGuard;
    }

    @Override
    public void setInteractiveAPI(
            InteractiveGuard guard) {
        this.interactiveGuard = guard;
    }

    @Override
    public ContainerGuard<P> getContainerAPI() {
        return containerGuard;
    }

    @Override
    public void setContainerAPI(ContainerGuard<P> guard) {
        this.containerGuard = guard;
    }

    @Override
    public Truck<P> getElement() {
        return truck;
    }

}
