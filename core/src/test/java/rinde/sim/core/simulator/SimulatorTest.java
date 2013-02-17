/**
 * 
 */
package rinde.sim.core.simulator;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import rinde.sim.core.dummies.Dummy2Data;
import rinde.sim.core.dummies.Dummy2User;
import rinde.sim.core.dummies.DummyTickListener;
import rinde.sim.core.dummies.DummyUser;
import rinde.sim.core.dummies.DummyUserAgent;
import rinde.sim.core.model.simulator.SimulatorModel;
import rinde.sim.core.simulation.Simulator;
import rinde.sim.core.simulation.TickListener;
import rinde.sim.core.simulation.TimeInterval;

/**
 * Make sure you run this test suite with assertions enabled !! (argument -ea to VM)
 * 
 * To do this automatically in Eclipse for every newly created launch test:
 *  Windows > Preferences > Junit > Add '-ea' to ...
 * 
 * 
 * @author Rinde van Lon (rinde.vanlon@cs.kuleuven.be)
 * @author dmerckx
 */
public class SimulatorTest {

	private final long timeStep = 100L;
	private Simulator simulator;

	@Before
	public void setUp() {
	    simulator = new Simulator(timeStep);
		Simulator.SimulatorEventType.valueOf("STOPPED");// just for test coverage of the
												// enum
	}

    @Test(expected = AssertionError.class)
    public void testRegisterNull() {
        simulator.registerUser(null);
    }
	
	@Test
	public void testRegisterTicklistener(){
	    simulator.configure();
	    simulator.registerTickListener(new DummyTickListener());
	}
	
	@Test(expected = AssertionError.class)
	public void TestRegisterTickListenerTooEarly(){
	    simulator.registerTickListener(new DummyTickListener());
	}
	
	@Test
	public void testRegisterModel(){
	    simulator.registerModel(new DummyModel());
	}

    @Test(expected = AssertionError.class)
    public void testRegisterModelTooLate() {
        simulator.configure();
        simulator.registerModel(new DummyModel());
    }
    
    @Test
    public void testRegisterUser(){
        simulator.configure();
        simulator.registerUser(new DummyUser());
    }
    
    @Test
    public void testRegisterWithData(){
        simulator.configure();
        Dummy2User user = new Dummy2User(0);
        Dummy2Data data = new Dummy2Data();
        simulator.registerUser(new Dummy2User(0), data);
        assertTrue(user.initData == null);
    }
    
    @Test(expected = AssertionError.class)
    public void testRegisterWithNullData(){
        simulator.configure();
        Dummy2User user = new Dummy2User(0);
        Dummy2Data data = new Dummy2Data();
        simulator.registerUser(new Dummy2User(0), null);
    }
    
    @Test
    public void testRegisterWithDataAndModel(){
        Dummy2Model model = new Dummy2Model();
        simulator.registerModel(model);
        assertEquals(0, model.objs.size());
        simulator.configure();
        Dummy2User user = new Dummy2User();
        Dummy2Data data = new Dummy2Data();
        simulator.registerUser(user, data);
        assertEquals(1, model.objs.size());
        assertEquals(data, user.initData);
    }
    

    @Test(expected = AssertionError.class)
    public void testRegisterUserTooEarly() {
        simulator.registerUser(new DummyUser());
    }
	
	@Test
	public void testTicks() {
		assertEquals(0L, simulator.getCurrentTime());
		
		DummyModel model = new DummyModel();
		simulator.registerModel(model);
		
        simulator.configure();
        
		DummyUserAgent user = new DummyUserAgent();
		DummyTickListener tl = new DummyTickListener();
		
		simulator.registerTickListener(tl);
		simulator.registerUser(user);
		
		simulator.advanceTick();
        simulator.advanceTick();
        
		assertEquals(200L, simulator.getCurrentTime());
		
		assertEquals(2, model.getTickCount());
		assertEquals(2, user.ticks);
		assertEquals(2, tl.getTickCount());
		
		simulator.unregisterUser(user);
		simulator.unregisterTickListener(tl);
		
		simulator.advanceTick();
        
        assertEquals(3, model.getTickCount());
        assertEquals(2, tl.getTickCount());
	}

	@Test
	public void testDefaultSimulatorModelIsPresent(){
	    assertEquals(1, simulator.getModels().size());
	    assertThat(simulator.getModels().get(0), instanceOf(SimulatorModel.class));
	}

	@Test
	public void testRegisterModels() {
        assertEquals(1, simulator.getModels().size());
        
		DummyModel m1 = new DummyModel();
		DummyModel m2 = new DummyModel();
		simulator.registerModel(m1);
		simulator.registerModel(m2);
		
		assertEquals(m1, simulator.getModels().get(1));
        assertEquals(m2, simulator.getModels().get(2));
	}

	@Test(expected = AssertionError.class)
	public void testUnregisterNull() {
		simulator.unregisterUser(null);
	}

	@Test(expected = AssertionError.class)
	public void testUnregisterTooEarly() {
		simulator.unregisterTickListener(new DummyTickListener());
	}

	@Test(expected = AssertionError.class)
	public void testStartWithoutConfiguring() {
		simulator.start();
	}

	@Test
	public void testStart() {
		simulator.configure();
		LimitingTickListener ltl = new LimitingTickListener(simulator, 3);
		simulator.registerTickListener(ltl);
		simulator.start();
		assertTrue(simulator.getCurrentTime() == 3 * timeStep);

		simulator.togglePlayPause();
		assertTrue(simulator.getCurrentTime() == 6 * timeStep);
		simulator.resetTime();
		assertTrue(simulator.getCurrentTime() == 0);
	}
	
	class LimitingTickListener implements TickListener {
		private final int limit;
		private int tickCount;
		private final Simulator sim;

		public LimitingTickListener(Simulator s, int tickLimit) {
			sim = s;
			limit = tickLimit;
			tickCount = 0;
		}

		public void reset() {
			tickCount = 0;
		}
		
		@Override
		public void tick(TimeInterval tl) {
			tickCount++;
		    if (tickCount >= limit) {
				assertTrue(sim.isPlaying());
				if (tl.getStartTime() > limit * tl.getTimeStep()) {
					sim.togglePlayPause();
				}
				sim.stop();
				assertFalse(sim.isPlaying());
				reset();
			}
		}
	}
}
