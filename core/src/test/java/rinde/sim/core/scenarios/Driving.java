package rinde.sim.core.scenarios;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;

import rinde.sim.core.graph.Point;
import rinde.sim.core.model.Agent;
import rinde.sim.core.model.interaction.InteractionModel;
import rinde.sim.core.model.pdp.Parcel;
import rinde.sim.core.model.pdp.PdpModel;
import rinde.sim.core.model.pdp.apis.PickupAPI.PickupState;
import rinde.sim.core.model.pdp.twpolicy.LiberalPolicy;
import rinde.sim.core.model.pdp.users.PickupPoint;
import rinde.sim.core.model.pdp.users.PickupPointData;
import rinde.sim.core.model.pdp.users.Truck;
import rinde.sim.core.model.pdp.users.TruckData;
import rinde.sim.core.model.road.PlaneRoadModel;
import rinde.sim.core.model.road.RoadModel;
import rinde.sim.core.model.road.apis.MovingRoadGuard;
import rinde.sim.core.model.road.users.MovingRoadData;
import rinde.sim.core.model.road.users.MovingRoadUser;
import rinde.sim.core.model.road.users.RoadData;
import rinde.sim.core.simulation.Simulator;
import rinde.sim.core.simulation.TimeLapse;
import rinde.sim.util.TimeWindow;

public class Driving {
    
    public static final int DRIVERS = 100;
    
    Simulator simulator;
    List<Driver> drivers;
    
    PickupPoint pickup;
    
    public void setupSim() {
        final RandomGenerator rng = new MersenneTwister(123);
        final RoadModel roadModel = new PlaneRoadModel(new Point(0,0), new Point(100,100), false, Double.MAX_VALUE);
      
        simulator = new Simulator(10000);
        
        simulator.registerModel(roadModel);
        simulator.configure();
    
        drivers = Lists.newArrayList();
        for(int i =0; i < DRIVERS; i++){
            drivers.add(new Driver());
        }
        
        for(Driver driver: drivers) {
            simulator.registerUser(
                    driver,
                    new MovingRoadData.Std(roadModel.getRandomPosition(rng), 10));
        }
    }
    
    @Test
    public void testRandomDriving() {
        setupSim();
        simulator.advanceTicks(50);
        List<Driver> drivers1 = drivers;
        
        setupSim();
        simulator.advanceTicks(50);
        List<Driver> drivers2 = drivers;
        
        assertTrue(drivers1.size() == drivers2.size());
        for(int i = 0; i < drivers1.size(); i++){
            assertEquals(drivers1.get(i).getRoadState().getLocation(),
                    drivers2.get(i).getRoadState().getLocation());
        }
    }
}


class Driver extends MovingRoadUser.Std implements Agent{

    @Override
    public void tick(TimeLapse time) {
        if(!roadAPI.isDriving()){
            Point target = roadAPI.getRandomLocation();
            roadAPI.setTarget(target);
        }
        
        roadAPI.advance(time);
    }
}
