package rinde.sim.core.model.pdp2.users;

import rinde.sim.core.model.pdp2.apis.TruckAPI;
import rinde.sim.core.model.road.users.MovingRoadUser;
import rinde.sim.core.simulation.types.Agent;

public interface Truck extends MovingRoadUser, Agent{
   
    public void initTruck(TruckAPI api);
}
