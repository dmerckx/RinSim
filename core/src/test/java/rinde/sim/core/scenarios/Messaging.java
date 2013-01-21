package rinde.sim.core.scenarios;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;

import rinde.sim.core.graph.Point;
import rinde.sim.core.model.Agent;
import rinde.sim.core.model.communication.Address;
import rinde.sim.core.model.communication.CommunicationModel;
import rinde.sim.core.model.communication.Delivery;
import rinde.sim.core.model.communication.Message;
import rinde.sim.core.model.communication.users.SimpleCommData;
import rinde.sim.core.model.communication.users.SimpleCommUser;
import rinde.sim.core.model.interaction.InteractionModel;
import rinde.sim.core.model.pdp.Parcel;
import rinde.sim.core.model.pdp.PdpModel;
import rinde.sim.core.model.pdp.apis.PickupAPI.PickupState;
import rinde.sim.core.model.pdp.twpolicy.LiberalPolicy;
import rinde.sim.core.model.pdp.users.PickupPoint;
import rinde.sim.core.model.pdp.users.PickupPointData;
import rinde.sim.core.model.pdp.users.TruckData;
import rinde.sim.core.model.road.PlaneRoadModel;
import rinde.sim.core.model.road.RoadModel;
import rinde.sim.core.simulation.Simulator;
import rinde.sim.core.simulation.TimeLapse;
import rinde.sim.util.TimeWindow;

public class Messaging {
    
    public static final int MESSAGERS = 6;
    private static double RELIABILITY = 1.0d;
    
    Simulator simulator;
    List<Messager> messagers;
    
    @Before
    public void setup() {
        CommunicationModel commModel = new CommunicationModel();
        simulator = new Simulator(1, 15);
        
        simulator.registerModel(commModel);
        simulator.configure();
        
        messagers = Lists.newArrayList();
        
        for(int i = 0; i < MESSAGERS; i++){
            messagers.add(new Messager(19 * i));
        }
 
        for(Messager messager: messagers) {
            simulator.registerUser(
                    messager,
                    new SimpleCommData.Std(RELIABILITY));
        }
        
        simulator.advanceTick();

        List<Address> addresses = Lists.newArrayList();
        for(Messager messager: messagers){
            addresses.add(messager.getCommunicationState().getAddress());
        }
        for(Messager messager: messagers){
            messager.setAddresses(addresses);
        }
    }

    @Test
    public void tryOneTick() {
        simulator.advanceTicks(6);
        List<Messager> messagers1 = messagers;
        
        setup();
        simulator.advanceTicks(6);
        List<Messager> messagers2 = messagers;
        
        for(int i = 0; i < messagers1.size(); i++){
            List<Msg> msgs1 = messagers1.get(i).getAllMessages();
            List<Msg> msgs2 = messagers2.get(i).getAllMessages();
            
            assertEquals(msgs1.size(), msgs2.size());
            for(int m = 0; m < msgs1.size(); m++){
                assertEquals(msgs1.get(m).code, msgs2.get(m).code);    
            }
        }
    }
    
    @Test
    public void tryMultipleTicks() {
        simulator.advanceTicks(30);
        List<Messager> messagers1 = messagers;
        
        setup();
        simulator.advanceTicks(30);
        List<Messager> messagers2 = messagers;
        
        for(int i = 0; i < messagers1.size(); i++){
            List<Msg> msgs1 = messagers1.get(i).getAllMessages();
            List<Msg> msgs2 = messagers2.get(i).getAllMessages();
            
            assertEquals(msgs1.size(), msgs2.size());
            for(int m = 0; m < msgs1.size(); m++){
                assertEquals(msgs1.get(m).code, msgs2.get(m).code);    
            }
        }
    }

    @Test
    public void testWithoutFullReliability(){
        RELIABILITY = 0.5d;
        
        setup();
        simulator.advanceTicks(7);
        List<Messager> messagers1 = messagers;
        
        setup();
        simulator.advanceTicks(7);
        List<Messager> messagers2 = messagers;
        
        for(int i = 0; i < messagers1.size(); i++){
            List<Msg> msgs1 = messagers1.get(i).getAllMessages();
            List<Msg> msgs2 = messagers2.get(i).getAllMessages();
            
            assertEquals(msgs1.size(), msgs2.size());
            for(int m = 0; m < msgs1.size(); m++){
                assertEquals(msgs1.get(m).code, msgs2.get(m).code);    
            }
        }
    }
}

class Messager extends SimpleCommUser.Std implements Agent{
    private List<Address> targets;
    private final Random rnd;
    
    public Messager(long seed) {
        this.rnd = new Random(seed);
    }
    
    public void setAddresses(List<Address> targets){
        this.targets = targets;
    }
    
    @Override
    public void tick(TimeLapse time) {
        if(time.getStartTime() < 5)
            return;
        
        Address target = targets.get(rnd.nextInt(targets.size()));
        commAPI.send(target, new Msg(rnd.nextInt()));
    }
    
    public List<Msg> getAllMessages(){
        Iterator<Delivery> it = commAPI.getMessages();
        List<Msg> result = new ArrayList<Msg>();
        
        while(it.hasNext()){
            result.add((Msg) it.next().message);
        }
        return result;
    }
    
    @Override
    public String toString() {
        return "Messager|" + commAPI.getAddress().toString();
    }
}

class Msg extends Message{
    public final int code;
    
    public Msg(int code){
        this.code = code;
    }
    
    @Override
    public String toString() {
        return "msg:"  + code;
    }
}