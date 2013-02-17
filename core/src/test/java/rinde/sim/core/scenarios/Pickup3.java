package rinde.sim.core.scenarios;

import static org.junit.Assert.*;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;
import org.junit.Test;

import rinde.sim.core.graph.Graph;
import rinde.sim.core.graph.MultiAttributeData;
import rinde.sim.core.graph.Point;
import rinde.sim.core.model.Agent;
import rinde.sim.core.model.interaction.InteractionModel;
import rinde.sim.core.model.pdp.Parcel;
import rinde.sim.core.model.pdp.PdpModel;
import rinde.sim.core.model.pdp.twpolicy.LiberalPolicy;
import rinde.sim.core.model.pdp.users.DeliveryPoint;
import rinde.sim.core.model.pdp.users.DeliveryPointData;
import rinde.sim.core.model.pdp.users.PickupPoint;
import rinde.sim.core.model.pdp.users.PickupPointData;
import rinde.sim.core.model.pdp.users.Truck;
import rinde.sim.core.model.pdp.users.TruckData;
import rinde.sim.core.model.road.GraphRoadModel;
import rinde.sim.core.model.road.RoadModel;
import rinde.sim.core.simulation.Simulator;
import rinde.sim.core.simulation.TimeLapse;
import rinde.sim.serializers.DotGraphSerializer;
import rinde.sim.serializers.SelfCycleFilter;
import rinde.sim.util.TimeWindow;

public class Pickup3 {

    private static final int STEP = 10000;
    public static final int TRUCKS = 10;
    public static final int PACKAGES = 50;
    
    Truck someTruck;
    Simulator simulator;
    
    public void setupSim() throws FileNotFoundException, IOException {
        final String MAP_DIR = "../core/files/maps/";
        // create a new simulator, load map of Leuven
        final RandomGenerator rng = new MersenneTwister(123);
        simulator = new Simulator(STEP);
        final Graph<MultiAttributeData> graph = DotGraphSerializer
                .getMultiAttributeGraphSerializer(new SelfCycleFilter()).read(MAP_DIR + "leuven-simple.dot");
        final RoadModel roadModel = new GraphRoadModel(graph);
        final InteractionModel interModel = new InteractionModel();
        final PdpModel pdpModel = new PdpModel(new LiberalPolicy());
        
        simulator.registerModel(roadModel);
        simulator.registerModel(interModel);
        simulator.registerModel(pdpModel);
        simulator.configure();
        
        int randomTruckNr = rng.nextInt(TRUCKS);
        for (int i = 0; i < TRUCKS; i++) {
            PdpTruck truck = new PdpTruck();
            simulator.registerUser(
                    truck,
                    new TruckData.Std(1000, roadModel.getRandomPosition(rng), 100));
            
            if(i == randomTruckNr){
                someTruck = truck;
            }
        }

        for (int i = 0; i < PACKAGES; i++) {
            Point from = roadModel.getRandomPosition(rng);
            Point to = roadModel.getRandomPosition(rng);
            final Parcel parcel =
                    new Parcel(from, to, STEP * 3, TimeWindow.ALWAYS, STEP * 2, TimeWindow.ALWAYS, 1);
            
            //Register pickup point
            simulator.registerUser(new PickupPoint.Std(), new PickupPointData.Std(parcel));
            
            //Register delivery point
            simulator.registerUser(new DeliveryPoint.Std(), new DeliveryPointData.Std(parcel));
        }
        
        //simulator.start();
        //View.startGui(simulator, 1, new GraphRoadModelRenderer(), new PDPModelRenderer());

    }
    
    @Test
    public void testPositions() throws FileNotFoundException, IOException {
        setupSim();
        simulator.advanceTicks(5000);
        
        Point p1 = someTruck.getRoadState().getLocation();
        
        setupSim();
        simulator.advanceTicks(5000);
        
        assertEquals(p1, someTruck.getRoadState().getLocation());
    }
}

class PdpTruck extends Truck<TruckData> implements Agent{
    
    Parcel target = null;
    
    public PdpTruck() {
        super();
    }
    
    @Override
    public void tick(TimeLapse time) {
        roadAPI.advance(time);  //Drive as far as possible
        
        if(roadAPI.isDriving())
            return;
        
        if(containerAPI.getCurrentLoad().size() == 0){  //Search new parcel
            if(target != null){
                target = containerAPI.tryPickup(time);
                if(target != null){
                    roadAPI.setTarget(target.destination);
                }
            }
            else{
                target = truckAPI.findClosestAvailableParcel(time);
                roadAPI.setTarget(target!=null?target.location:roadAPI.getRandomLocation());
            }
        }
        else{
            target = containerAPI.tryDelivery(time);
        }
    }
}