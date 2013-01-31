package rinde.sim.core.model.pdp;

import static org.junit.Assert.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.List;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;

import rinde.sim.core.graph.Point;
import rinde.sim.core.model.Agent;
import rinde.sim.core.model.interaction.InteractionModel;
import rinde.sim.core.model.pdp.apis.ContainerAPI;
import rinde.sim.core.model.pdp.apis.PickupAPI;
import rinde.sim.core.model.pdp.apis.ContainerAPI.ContState;
import rinde.sim.core.model.pdp.apis.PickupAPI.PickupState;
import rinde.sim.core.model.pdp.apis.TruckAPI;
import rinde.sim.core.model.pdp.twpolicy.LiberalPolicy;
import rinde.sim.core.model.pdp.users.PickupPoint;
import rinde.sim.core.model.pdp.users.PickupPointData;
import rinde.sim.core.model.pdp.users.Truck;
import rinde.sim.core.model.pdp.users.TruckData;
import rinde.sim.core.model.pdp.users.PickupPoint.Std;
import rinde.sim.core.model.road.PlaneRoadModel;
import rinde.sim.core.model.road.RoadModel;
import rinde.sim.core.model.road.apis.MovingRoadAPI;
import rinde.sim.core.simulation.Simulator;
import rinde.sim.core.simulation.TimeLapse;
import rinde.sim.util.TimeWindow;
import rinde.sim.util.Tuple;

public class PDPTest {

    static final double EPSILON = 0.0000001;

    RandomGenerator rng;
    PdpModel pdpModel;
    Simulator sim;
    
    @Before
    public void setUp() {
        rng = new MersenneTwister(123);
        pdpModel = new PdpModel(new LiberalPolicy());
        RoadModel roadModel = new PlaneRoadModel(new Point(0,0), new Point(100,100), false, Double.MAX_VALUE);
        InteractionModel interModel = new InteractionModel();

        sim = new Simulator(1);
        
        sim.registerModel(roadModel);
        sim.registerModel(interModel);
        sim.registerModel(pdpModel);
        sim.configure();
    }

    @Test
    public void testStatesAtPickupForTruck() {
        Point pos = new Point(50, 50);
        //The parcel we use takes 2 turns to get picked up.
        Parcel parcel = new DummyParcel(pos, pos, 2, 0);
        
        PickupTruck t = new PickupTruck(5); //The actual truck
        Truck<?> tOutside = t;              //The view of this truck to external agents
        
        TestPickupPoint pp = new TestPickupPoint(); //The actual pickup point
        TestPickupPoint ppOutside = pp;             //The view of this pickup point to external agents
        
        //Note that the external view is captured at the start of a turn and is immutable thereafter.
        //The actual view COULD provide realtime date information to the actual agent.
        
        sim.registerUser(t, new DummyData(pos));
        sim.registerUser(pp, new PickupPointData.Std(parcel));
        
        //During this test we will monitor the internal and external state of the 2 involved agents
        //For a truck these will differ:
        //  * the internal state captures the state AFTER a turn (since we dont monitor during turns)
        //  * the external state captures the state AT THE START of a turn (and remains immutable thereafter)
        //For a pickup point these will NOT differ:
        //  * both the internal and external state will be equal and immutable during a turn
        int i = 0;
        for(int v:getViews(1, 10)){
            while(i < v){
                System.out.println("-----------------");
                System.out.println("transition from " + sim.getCurrentTime() + " to " + (sim.getCurrentTime() +1));
                sim.advanceTick();
                i++;
                System.out.println("-----------------");
                System.out.println("snapshot at time: " + sim.getCurrentTime());
            }
            assertEquals(pos, t.getRoadState().getLocation());
            if(v <= 4){
                //The trucks internal state before turn 5: normal state
                assertEquals(ContState.AVAILABLE,t.getContainerApi().getCurrentContState());
                assertEquals(0,t.getContainerApi().getCurrentLoad().size());
                assertEquals(1,t.getTimeLeft());
                
                //The trucks outside state: normal state
                assertEquals(ContState.AVAILABLE,tOutside.getContainerState().getContState());
                assertEquals(0,tOutside.getContainerState().getLoad().size());
                
                //Package inside/outside state: normal
                assertEquals(PickupState.AVAILABLE,pp.getPickupApi().getPickupState());
            }
            else if(v == 5){
                //~~ DURING THIS TURN THE PACKAGE WAS PICKED UP ~~
                
                //The trucks internal state at turn 5:
                // * it has turned into pickup up state during this turn
                // * all the time was spend on picking up the package
                // * the load is increased because of the new package
                assertEquals(ContState.PICKING_UP,t.getContainerApi().getCurrentContState());
                assertEquals(1,t.getContainerApi().getCurrentLoad().size());
                assertEquals(0,t.getTimeLeft());

                //The trucks outside state: normal state (Because it captures the START of the turn)
                assertEquals(ContState.AVAILABLE,tOutside.getContainerState().getContState());
                assertEquals(0,tOutside.getContainerState().getLoad().size());

                //Package inside/outside state: normal
                assertEquals(PickupState.AVAILABLE,pp.getPickupApi().getPickupState());
                assertEquals(PickupState.AVAILABLE,ppOutside.getPickupPointState().getPickupState());
            }
            else if(v == 6){
                //The trucks internal state at turn 6:
                // * picking up the package completed during this turn, hence back to state AVAILABLE
                // * all the time was spend on doing the last work on picking up the package
                // * the load is increased because of the new package
                assertEquals(ContState.AVAILABLE,t.getContainerApi().getCurrentContState());
                assertEquals(1,t.getContainerApi().getCurrentLoad().size());
                assertEquals(0,t.getTimeLeft());

                //The trucks outside state: PICKING_UP state (Because it captures the START of the turn)
                assertEquals(ContState.PICKING_UP,tOutside.getContainerState().getContState());
                assertEquals(1,tOutside.getContainerState().getLoad().size());

                //Package inside/outside state: normal
                assertEquals(PickupState.BEING_PICKED_UP,pp.getPickupApi().getPickupState());
                assertEquals(PickupState.BEING_PICKED_UP,ppOutside.getPickupPointState().getPickupState());
            }
            else {
                //The truck is done picking up the package
                assertEquals(ContState.AVAILABLE,t.getContainerApi().getCurrentContState());
                assertEquals(1,t.getContainerApi().getCurrentLoad().size());
                assertEquals(1,t.getTimeLeft());

                //The trucks outside state: done with pickup
                assertEquals(ContState.AVAILABLE,tOutside.getContainerState().getContState());
                assertEquals(1,tOutside.getContainerState().getLoad().size());

                //Package inside/outside state: normal
                assertEquals(PickupState.PICKED_UP,pp.getPickupApi().getPickupState());
                assertEquals(PickupState.PICKED_UP,ppOutside.getPickupPointState().getPickupState());
            }
        }
    }
    
    private List<Integer> getViews(int from, int to){
        List<Integer> result = Lists.newArrayList();
        
        for(int i = 0; i < to-from; i++){
            result.add(from + i);
        }
        
        Collections.sort(result);
        return result;
    }
    
    private List<Integer> getViews(int from, int to, int nr){
        List<Integer> result = Lists.newArrayList();
        
        for(int i = 0; i < nr; i++){
            result.add(rng.nextInt(to - from) + from);
        }
        
        Collections.sort(result);
        return result;
    }
}

class DummyParcel extends Parcel{
    public DummyParcel(Point from, Point to, long pPickupDuration, long pDeliveryDuration) {
        super(from, to, pPickupDuration, new TimeWindow(0, Long.MAX_VALUE), pDeliveryDuration, new TimeWindow(0, Long.MAX_VALUE),1);
    }
    
}

class TestPickupPoint extends PickupPoint.Std {
    
    public PickupAPI getPickupApi(){
        return pickupAPI;
    }
}

class PickupTruck extends Truck<TruckData> implements Agent{
    private int pickupTime;
    private long timeLeft;
    
    public PickupTruck(int pickupTime) {
        this.pickupTime = pickupTime;
        this.timeLeft = 0;
    }
    
    @Override
    public void tick(TimeLapse time) {
        if(time.getStartTime() == pickupTime){
            System.out.println(time.getStartTime() + " -> Try pickup !");
            Parcel p = containerAPI.tryPickup(time);
        }
        timeLeft = time.getTimeLeft();
    }
    
    public ContainerAPI getContainerApi(){
        return containerAPI;
    }
    
    public TruckAPI getTruckApi(){
        return truckAPI;
    }
    
    public MovingRoadAPI getRoadAPI(){
        return roadAPI;
    }
    
    public long getTimeLeft(){
        return timeLeft;
    }
}

class DummyData extends TruckData.Std{

    public DummyData(Point pos) {
        super(1, pos, 2);
    }
}