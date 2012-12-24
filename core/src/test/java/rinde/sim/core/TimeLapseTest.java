/**
 * 
 */
package rinde.sim.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import rinde.sim.core.simulation.TimeLapse;
import rinde.sim.core.simulation.time.TimeLapseHandle;

/**
 * @author Rinde van Lon (rinde.vanlon@cs.kuleuven.be)
 * 
 */
public class TimeLapseTest {

	@Test
	public void emptyConstructor() {
		TimeLapse tl = new TimeLapseHandle();
		assertEquals(0, tl.getCurrentTime());
	}

	@Test
	public void constructor() {
		TimeLapse tl = new TimeLapseHandle(0, 10);

		assertEquals(0, tl.getCurrentTime());
		assertEquals(0, tl.getTimeConsumed());
		assertEquals(10, tl.getTimeStep());
		assertEquals(10, tl.getTimeLeft());
		assertTrue(tl.hasTimeLeft());

	}

	@SuppressWarnings("unused")
	@Test(expected = IllegalArgumentException.class)
	public void constructorFail1() {
		new TimeLapseHandle(-1, 0);
	}

	@SuppressWarnings("unused")
	@Test(expected = IllegalArgumentException.class)
	public void constructorFail2() {
		new TimeLapseHandle(1, 0);
	}

	@Test
	public void consume1() {

		int[] start = { 0, 10, 100, 500 };
		int[] end = { 100, 1000, 113, 783 };

		for (int i = 0; i < start.length; i++) {
			TimeLapse tl = new TimeLapseHandle(start[i], end[i]);
			assertEquals(end[i] - start[i], tl.getTimeLeft());
			assertEquals(start[i], tl.getCurrentTime());
			assertEquals(0, tl.getTimeConsumed());
			assertTrue(tl.hasTimeLeft());
			assertEquals(end[i] - start[i], tl.getTimeStep());

			tl.consume(10);
			assertEquals(end[i] - start[i] - 10, tl.getTimeLeft());
			assertEquals(start[i] + 10, tl.getCurrentTime());
			assertEquals(10, tl.getTimeConsumed());
			assertTrue(tl.hasTimeLeft());
			assertEquals(end[i] - start[i], tl.getTimeStep());

			tl.consumeAll();
			assertEquals(0, tl.getTimeLeft());
			assertEquals(end[i], tl.getCurrentTime());
			assertEquals(end[i] - start[i], tl.getTimeConsumed());
			assertFalse(tl.hasTimeLeft());
			assertEquals(end[i] - start[i], tl.getTimeStep());
		}

	}

	@Test(expected = IllegalArgumentException.class)
	public void consumeFail1() {
		TimeLapse tl = new TimeLapseHandle(0, 10);
		tl.consume(-1);
	}

	@Test(expected = IllegalArgumentException.class)
	public void consumeFail2() {
		TimeLapse tl = new TimeLapseHandle(0, 10);
		tl.consume(11);
	}

}
