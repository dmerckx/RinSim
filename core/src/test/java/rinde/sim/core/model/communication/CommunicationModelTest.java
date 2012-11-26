package rinde.sim.core.model.communication;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.security.auth.callback.Callback;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import rinde.sim.core.TimeLapseFactory;
import rinde.sim.core.graph.Point;
import rinde.sim.core.model.communication.dummies.TestCommUnit;
import rinde.sim.core.model.communication.dummies.TestCommUser;
import rinde.sim.core.model.communication.supported.CommUnit;
import rinde.sim.core.model.communication.users.CommUser;
import rinde.sim.core.simulation.TimeInterval;
import rinde.sim.core.simulation.TimeLapse;

@RunWith(Parameterized.class)
public class CommunicationModelTest {

	private CommunicationModel model;
	private final Class<? extends CommunicationModel> type;
	private final double radius;
    private final boolean reverse;
	
	int time;
	public static int STEP = 100;
    TestCommUser sender1;
    TestCommUser sender2;
    TestCommUser recipient;
    TestCommUnit sender1Unit;
    TestCommUnit sender2Unit;
    TestCommUnit recipientUnit;
    
	public CommunicationModelTest(Class<? extends CommunicationModel> clazz, double rad, boolean rev) {
		type = clazz;
		radius = rad;
		reverse = rev;
	}

	@Before
	public void setUp() throws Exception {
		model = type.getConstructor().newInstance();
        
        sender1 = new TestCommUser(10);
        sender2 = new TestCommUser(10);
        recipient = new TestCommUser(10);
        sender1Unit = sender1.buildUnit();
        sender2Unit = sender2.buildUnit();
        recipientUnit = recipient.buildUnit();
        model.register(sender1Unit);
        model.register(sender2Unit);
        model.register(recipientUnit);
        sender1Unit.init();
        sender2Unit.init();
        recipientUnit.init();
	}

	@Parameters
	public static List<Object[]> parameters() {
		return Arrays.asList(new Object[][] {
		        { CommunicationModel.class, 5, false },
                { CommunicationModel.class, 5, true },
                { CommunicationModel.class, 50, false },
				{ CommunicationModel.class, 50, true }});
		// return Arrays.asList(new Object[][]{ {CommunicationModel2.class}});
	}

	@Test
	public void testRegister() {
	    assertEquals(3, model.comms.size());
	    model.unregister(sender1Unit);
        model.unregister(sender2Unit);
        assertEquals(1, model.comms.size());
        model.unregister(recipientUnit);
        assertEquals(0, model.comms.size());
	    
		TestCommUser user = new TestCommUser(new Point(0, 10), 10, 1);
		model.register(user.buildUnit());
		user.unit.init();
		assertTrue(model.comms.containsKey(user.commAPI.getAddress()));
        assertEquals(1, model.comms.size());
	}

	@Test(expected = AssertionError.class)
	public void registerFail() {
		model.register(null);
	}

	@Test
	public void testUnregister() {
		TestCommUser user = new TestCommUser(new Point(0, 10), 10, 1);
		TestCommUnit unit = user.buildUnit();
		model.register(unit);
		unit.init();
		assertTrue(model.comms.containsKey(user.commAPI.getAddress()));
		
		model.unregister(unit);
		assertFalse(model.comms.containsKey(user.commAPI.getAddress()));
	}
	
	@Test
	public void testSimpleSend() {
		Message msg = new Message();
		
		sender1.commAPI.send(recipient.commAPI.getAddress(), msg);

		assertFalse(recipient.commAPI.getMessages().hasNext());
		
        tick();
       
        assertTrue(recipient.commAPI.getMessages().hasNext());
		assertEquals(msg,recipient.commAPI.getMessages().next().message);
	}

    /**
     * When a user X sends 2 messages M1 and M2 to Z then Z will
     * always receive the messages in the order they were send.
     */
	@Test
    public void testOrderedSameSender() throws Exception {
        Message msg1 = new Message();
        Message msg2 = new Message();
        
        sender1.commAPI.send(recipient.commAPI.getAddress(), msg1);
        sender1.commAPI.send(recipient.commAPI.getAddress(), msg2);

        tick();
        
        Iterator<Delivery> it = recipient.commAPI.getMessages();
        assertEquals(msg1, it.next().message);
        assertEquals(msg2, it.next().message);
        
        setUp();
        
        sender1.commAPI.send(recipient.commAPI.getAddress(), msg2);
        sender1.commAPI.send(recipient.commAPI.getAddress(), msg1);

        tick();
        
        it = recipient.commAPI.getMessages();
        assertEquals(msg2, it.next().message);
        assertEquals(msg1, it.next().message);
    }
	
	/**
	 * When users X and Y both send a message to Z then Z will
	 * always receive the message from X first OR it will
	 * always receive the message from Y first.
	 * The order in which the messages were send should not matter.
	 */
	@Test
    public void testDeterminism2Senders() throws Exception {
        Message msg1 = new Message();
        Message msg2 = new Message();
        
        sender1.commAPI.send(recipient.commAPI.getAddress(), msg1);
        sender2.commAPI.send(recipient.commAPI.getAddress(), msg2);

        tick();
        
        Iterator<Delivery> it = recipient.commAPI.getMessages();
        Message receivedFirst = it.next().message;
        Message receivedLast = it.next().message;
        
        setUp();
        
        sender2.commAPI.send(recipient.commAPI.getAddress(), msg2);
        sender1.commAPI.send(recipient.commAPI.getAddress(), msg1);

        tick();
        
        it = recipient.commAPI.getMessages();
        assertEquals(receivedFirst, it.next().message);
        assertEquals(receivedLast, it.next().message);
	}

	//TODO test broadcasting
	//TODO test reliability

	@Test
	public void testGetSupportedType() {
		assertEquals(CommUnit.class, model.getSupportedType());
	}

    private void tick(){
        TimeLapse tl1 = TimeLapseFactory.create(time, time + STEP);
        TimeLapse tl2 = TimeLapseFactory.create(time, time + STEP);
        TimeLapse tl3 = TimeLapseFactory.create(time, time + STEP);
        TimeInterval ti = TimeLapseFactory.create(time, time + STEP);
        
        model.tick(ti);
        
        if(reverse){
            sender1Unit.tick(tl1);
            sender2Unit.tick(tl2);
            recipientUnit.tick(tl3);
        }else{
            recipientUnit.tick(tl3);
            sender2Unit.tick(tl1);
            sender1Unit.tick(tl2);
        }
        
        if(reverse){
            sender1Unit.afterTick(ti);
            sender2Unit.afterTick(ti);
            recipientUnit.afterTick(ti);
        }else{
            recipientUnit.afterTick(ti);
            sender2Unit.afterTick(ti);
            sender1Unit.afterTick(ti);
        }
        time += STEP;
    }
}
