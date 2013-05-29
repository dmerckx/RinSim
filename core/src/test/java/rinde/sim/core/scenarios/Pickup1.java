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
import rinde.sim.core.simulation.Simulator;
import rinde.sim.core.simulation.TimeLapse;
import rinde.sim.core.simulation.policies.Policies;
import rinde.sim.core.simulation.policies.execution.ModPoolBatchRecursive;
import rinde.sim.util.TimeWindow;

public class Pickup1 {
    
    public static final int SECONDARY_TRUCKS = 1000;
    
    Simulator simulator;
    List<TestTruck1> trucks;
    
    PickupPoint pickup;
    
    @Before
    public void setup() {
        final RandomGenerator rng = new MersenneTwister(123);
        final RoadModel roadModel = new PlaneRoadModel(new Point(0,0), new Point(100,100), false, Double.MAX_VALUE);
        final InteractionModel interModel = new InteractionModel();
        final PdpModel pdpModel = new PdpModel(new LiberalPolicy());

        simulator = new Simulator(1, Policies.getModPool(10, 5, false));
        
        simulator.registerModel(roadModel);
        simulator.registerModel(interModel);
        simulator.registerModel(pdpModel);
        simulator.configure();

        Point targetPos = new Point(50,50);
        
        trucks = new ArrayList<TestTruck1>();
        for(int i =0; i < SECONDARY_TRUCKS; i++){
            trucks.add(new TestTruck1(targetPos, rng.nextBoolean()? 2:0, i));
        }
        pickup = new PickupPoint.Std();
       
        //Register secondary trucks that should all come late
        for(TestTruck1 secondary: trucks) {
            simulator.registerUser(
                    secondary,
                    new TruckData.Std(Double.POSITIVE_INFINITY, roadModel.getRandomPosition(rng), 100));
        }
        
        //Register the pickup point to which all trucks will drive
        Parcel p = new Parcel(targetPos, new Point(60,60), 10, TimeWindow.ALWAYS, 10, TimeWindow.ALWAYS, 10.0);
        simulator.registerUser(pickup, new PickupPointData.Std(p));
    }

    @Test
    public void testPickup() {
        simulator.advanceTicks(6);
        
        TestTruck1 truck1 = null;
        for(TestTruck1 secondary:trucks){
            if(secondary.didPickup()){
                assertTrue(truck1 == null);
                truck1 = secondary;
            }
        }
        assertTrue(truck1 != null);
        
        //reset evrything
        setup();
        simulator.advanceTicks(6);
        
        TestTruck1 truck2 = null;
        for(TestTruck1 secondary:trucks){
            if(secondary.didPickup()){
                assertTrue(truck2 == null);
                truck2 = secondary;
            }
        }
        assertTrue(truck2 != null);
        
        assertEquals(truck1.nr, truck2.nr);
        
        System.out.println("nr: " + truck1.nr);
    }
    
    @Test
    public void testPickupPointDelivered() {
        simulator.advanceTicks(1);
        assertEquals(PickupState.AVAILABLE, pickup.getPickupPointState().getPickupState());
        simulator.advanceTicks(4);
        assertEquals(PickupState.AVAILABLE, pickup.getPickupPointState().getPickupState());
        simulator.advanceTicks(1);
        assertEquals(PickupState.BEING_PICKED_UP, pickup.getPickupPointState().getPickupState());
        simulator.advanceTicks(8);
        assertEquals(PickupState.BEING_PICKED_UP, pickup.getPickupPointState().getPickupState());
        simulator.advanceTicks(1);
        assertEquals(PickupState.PICKED_UP, pickup.getPickupPointState().getPickupState());
        simulator.advanceTicks(20);
        assertEquals(PickupState.PICKED_UP, pickup.getPickupPointState().getPickupState());
    }
}


class TestTruck1 extends Truck<TruckData> implements Agent{
    private Point target;
    private int slowdown;
    private long turnEnd;
    public final int nr;

    public TestTruck1(Point target, int nr) {
        this(target, 0, nr);
        turnEnd = 0;
    }
    
    public TestTruck1(Point target, int slowdownMilli, int nr) {
        this.target = target;
        this.slowdown = slowdownMilli;
        this.nr = nr;
    }
    
    @Override
    public void tick(TimeLapse time) {
        if(time.getStartTime() < 5)
            return;
        
        if(time.getStartTime() == 5){
            try {
                Thread.sleep(slowdown);
            } catch (InterruptedException e) {
                e.printStackTrace();
                throw new RuntimeException();
            }
            roadAPI.setTarget(target);
        }
        
        roadAPI.advance(time);
        if(!roadAPI.isDriving()){
            containerAPI.tryPickup(time);
        }
        
        turnEnd = System.nanoTime();
    }
    
    public boolean didPickup(){
        return containerAPI.getCurrentLoad().size() > 0;
    }
    
    public long endOfTurn(){
        return turnEnd;
    }
}
