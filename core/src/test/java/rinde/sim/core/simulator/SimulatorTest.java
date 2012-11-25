/**
 * 
 */
package rinde.sim.core.simulator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.*;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import rinde.sim.core.dummies.DummyModel;
import rinde.sim.core.dummies.DummyObject;
import rinde.sim.core.dummies.DummyTickListener;
import rinde.sim.core.dummies.DummyUnit;
import rinde.sim.core.dummies.DummyUser;
import rinde.sim.core.model.Agent;
import rinde.sim.core.model.simulator.SimulatorModel;
import rinde.sim.core.model.simulator.apis.SimulatorAPI;
import rinde.sim.core.model.simulator.users.SimulatorUser;
import rinde.sim.core.simulation.Simulator;
import rinde.sim.core.simulation.TickListener;
import rinde.sim.core.simulation.TimeInterval;
import rinde.sim.core.simulation.TimeLapse;

import com.google.common.collect.Sets;

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
        simulator.register(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRegisterUnknowType() {
        simulator.register(new DummyObject());
    }
	
	@Test
	public void testRegisterTicklistener(){
	    simulator.configure();
	    simulator.register(new DummyTickListener());
	}
	
	@Test(expected = AssertionError.class)
	public void TestRegisterTickListenerTooEarly(){
	    simulator.register(new DummyTickListener());
	}
	
	@Test
	public void testRegisterModel(){
	    simulator.register(new DummyModel());
	}

    @Test(expected = AssertionError.class)
    public void testRegisterModelTooLate() {
        simulator.configure();
        simulator.register(new DummyModel());
    }
    
    @Test
    public void testRegisterUser(){
        simulator.configure();
        simulator.register(new DummyUser());
    }

    @Test(expected = AssertionError.class)
    public void testRegisterUserTooEarly() {
        simulator.register(new DummyUser());
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testRegisterUnitDirectly(){
        simulator.register(new DummyUnit(new DummyUser()));
    }
	
	@Test
	public void testTicks() {
		assertEquals(0L, simulator.getCurrentTime());
		
		DummyModel model = new DummyModel();
		simulator.register(model);
		
        simulator.configure();
        
		DummyUser user = new DummyUser();
		DummyUnit unit = user.getUnit();
		DummyTickListener tl = new DummyTickListener();
		
		simulator.register(tl);
		simulator.register(user);
		
		simulator.advanceTick();
        simulator.advanceTick();
        
		assertEquals(200L, simulator.getCurrentTime());
		
		assertEquals(2, model.getTickCount());
		assertEquals(2, unit.getTickCount());
		assertEquals(2, tl.getTickCount());
		
		simulator.unregister(user);
		simulator.unregister(tl);
		
		simulator.advanceTick();
        
        assertEquals(3, model.getTickCount());
        assertEquals(2, unit.getTickCount());
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
		simulator.register(m1);
		simulator.register(m2);
		
		assertEquals(m1, simulator.getModels().get(1));
        assertEquals(m2, simulator.getModels().get(2));
	}

	@Test(expected = AssertionError.class)
	public void testUnregisterNull() {
		simulator.unregister(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testUnregisterModel() {
		simulator.unregister(new DummyModel());
	}

	@Test(expected = AssertionError.class)
	public void testUnregisterTooEarly() {
		simulator.unregister(new DummyTickListener());
	}

	@Test(expected = AssertionError.class)
	public void testStartWithoutConfiguring() {
		simulator.start();
	}

	@Test
	public void testStart() {
		simulator.configure();
		LimitingTickListener ltl = new LimitingTickListener(simulator, 3);
		simulator.register(ltl);
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
