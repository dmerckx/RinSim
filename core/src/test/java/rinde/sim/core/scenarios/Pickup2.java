package rinde.sim.core.scenarios;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;
import org.junit.Test;

import rinde.sim.core.graph.Point;
import rinde.sim.core.model.Agent;
import rinde.sim.core.model.interaction.InteractionModel;
import rinde.sim.core.model.pdp.Parcel;
import rinde.sim.core.model.pdp.PdpModel;
import rinde.sim.core.model.pdp.apis.DeliveryAPI.DeliveryState;
import rinde.sim.core.model.pdp.apis.PickupAPI.PickupState;
import rinde.sim.core.model.pdp.twpolicy.LiberalPolicy;
import rinde.sim.core.model.pdp.users.DeliveryPoint;
import rinde.sim.core.model.pdp.users.DeliveryPointData;
import rinde.sim.core.model.pdp.users.PickupPoint;
import rinde.sim.core.model.pdp.users.PickupPointData;
import rinde.sim.core.model.pdp.users.Truck;
import rinde.sim.core.model.pdp.users.TruckData;
import rinde.sim.core.model.road.PlaneRoadModel;
import rinde.sim.core.model.road.RoadModel;
import rinde.sim.core.simulation.Simulator;
import rinde.sim.core.simulation.TimeLapse;
import rinde.sim.core.simulation.policies.Policies;
import rinde.sim.core.simulation.policies.agents.MultiThreaded;
import rinde.sim.core.simulation.policies.agents.areas2.Areas2;
import rinde.sim.util.TimeWindow;

import com.google.common.collect.Lists;

public class Pickup2 {
    public static final int DRIVERS = 500;
    public static final int PACKAGES = 50;
    
    Simulator simulator;
    List<TestTruck2> trucks;
    List<PickupPoint.Std> pickupPoints;
    List<DeliveryPoint.Std> deliveryPoints;
    
    public void setupSim() {
        final RandomGenerator rng = new MersenneTwister(123);
        final RoadModel roadModel = new PlaneRoadModel(new Point(0,0), new Point(100,100), false, Double.MAX_VALUE);
        final InteractionModel interModel = new InteractionModel();
        final PdpModel pdpModel = new PdpModel(new LiberalPolicy());
      
        //simulator = new Simulator(1, new Areas2(10, 3, 4));
        simulator = new Simulator(1, Policies.getModPool(5, 5, true));
        
        
        
        simulator.registerModel(roadModel);
        simulator.registerModel(interModel);
        simulator.registerModel(pdpModel);
        simulator.configure();
    
        trucks = Lists.newArrayList();
        pickupPoints = Lists.newArrayList();
        deliveryPoints = Lists.newArrayList();
        
        for(int i =0; i < DRIVERS; i++){
            trucks.add(new TestTruck2());
        }
        for(int i =0; i < PACKAGES; i++){
            pickupPoints.add(new PickupPoint.Std());
            deliveryPoints.add(new DeliveryPoint.Std());
        }
        
        for(TestTruck2 truck: trucks) {
            simulator.registerUser(
                    truck,
                    new TruckData.Std(Double.MAX_VALUE, roadModel.getRandomPosition(rng), 10));
        }
        
        for(int i =0; i < PACKAGES; i++) {
            Parcel p =
                    new Parcel(roadModel.getRandomPosition(rng), roadModel.getRandomPosition(rng),
                                10, TimeWindow.ALWAYS,
                                10, TimeWindow.ALWAYS,
                                10.0);
            simulator.registerUser(
                    pickupPoints.get(i),
                    new PickupPointData.Std(p));
            simulator.registerUser(
                    deliveryPoints.get(i),
                    new DeliveryPointData.Std(p));
        }
    }
    
    @Test
    public void snapshotTest1() {
        setupSim();
        simulator.advanceTicks(500);
        List<TestTruck2> drivers1 = trucks;
        List<PickupPoint.Std> pickupPoints1 = pickupPoints;
        List<DeliveryPoint.Std> deliveryPoints1 = deliveryPoints;
        
        setupSim();
        simulator.advanceTicks(500);
        List<TestTruck2> drivers2 = trucks;
        List<PickupPoint.Std> pickupPoints2 = pickupPoints;
        List<DeliveryPoint.Std> deliveryPoints2 = deliveryPoints;
        
        assertTrue(drivers1.size() == drivers2.size());
        for(int i = 0; i < drivers1.size(); i++){
            assertEquals(drivers1.get(i).getRoadState().getLocation(),
                    drivers2.get(i).getRoadState().getLocation());
            
            assertEquals(drivers1.get(i).getContainerState().getLoad().size(),
                    drivers2.get(i).getContainerState().getLoad().size());
            assertEquals(drivers1.get(i).getContainerState().getContState(),
                    drivers2.get(i).getContainerState().getContState());
        }
        

        for(int i = 0; i < PACKAGES; i++){
            assertEquals(pickupPoints1.get(i).getPickupPointState().getPickupState(),
                    pickupPoints2.get(i).getPickupPointState().getPickupState());
            assertEquals(deliveryPoints1.get(i).getDeliveryPointState().getDeliveryState(),
                    deliveryPoints2.get(i).getDeliveryPointState().getDeliveryState());
        }
    }
    
    @Test
    public void snapshotTest2() {
        setupSim();
        simulator.advanceTicks(4);
        List<TestTruck2> drivers1 = trucks;
        List<PickupPoint.Std> pickupPoints1 = pickupPoints;
        List<DeliveryPoint.Std> deliveryPoints1 = deliveryPoints;
        
        setupSim();
        simulator.advanceTicks(4);
        List<TestTruck2> drivers2 = trucks;
        List<PickupPoint.Std> pickupPoints2 = pickupPoints;
        List<DeliveryPoint.Std> deliveryPoints2 = deliveryPoints;
        
        assertTrue(drivers1.size() == drivers2.size());
        for(int i = 0; i < drivers1.size(); i++){
            assertEquals(drivers1.get(i).getRoadState().getLocation(),
                    drivers2.get(i).getRoadState().getLocation());
            
            assertEquals(drivers1.get(i).getContainerState().getLoad().size(),
                    drivers2.get(i).getContainerState().getLoad().size());
            assertEquals(drivers1.get(i).getContainerState().getContState(),
                    drivers2.get(i).getContainerState().getContState());
        }
        

        for(int i = 0; i < PACKAGES; i++){
            assertEquals(pickupPoints1.get(i).getPickupPointState().getPickupState(),
                    pickupPoints2.get(i).getPickupPointState().getPickupState());
            assertEquals(deliveryPoints1.get(i).getDeliveryPointState().getDeliveryState(),
                    deliveryPoints2.get(i).getDeliveryPointState().getDeliveryState());
        }
    }
    
    @Test
    public void completedTest() {
        setupSim();
        simulator.advanceTicks(50);
        List<TestTruck2> drivers1 = trucks;
        
        setupSim();
        simulator.advanceTicks(50);
        List<TestTruck2> drivers2 = trucks;
        
        assertTrue(drivers1.size() == drivers2.size());
        for(int i = 0; i < drivers1.size(); i++){
            assertEquals(drivers1.get(i).getRoadState().getLocation(),
                    drivers2.get(i).getRoadState().getLocation());
        }
        

        for(int i = 0; i < pickupPoints.size(); i++){
            assertTrue(pickupPoints.get(i).getPickupPointState().getPickupState() == PickupState.PICKED_UP);
            assertTrue(deliveryPoints.get(i).getDeliveryPointState().getDeliveryState() == DeliveryState.DELIVERED);
        }
    }
}

class TestTruck2 extends Truck<TruckData> implements Agent{
    Point target = null;
    
    @Override
    public void tick(TimeLapse time) {
        roadAPI.advance(time);  //Drive as far as possible
        
        if(roadAPI.isDriving())
            return;
        
        if(containerAPI.getCurrentLoad().size() == 0){  //Search new parcel
            if(target != null){
                Parcel p = containerAPI.tryPickup(time);
                if(p != null){
                    roadAPI.setTarget(p.destination);
                }
                else{
                    roadAPI.setTarget(roadAPI.getRandomLocation());
                }
            }
            else{
                target = truckAPI.findClosestAvailableParcel();
                roadAPI.setTarget(target!=null?target:roadAPI.getRandomLocation());
            }
        }
        else{
            containerAPI.tryDelivery(time);
            roadAPI.setTarget(roadAPI.getRandomLocation());
        }
    }
}
