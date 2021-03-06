/**
 * 
 */
package rinde.sim.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import rinde.sim.core.simulation.TimeLapse;
import rinde.sim.core.simulation.time.TimeIntervalImpl;
import rinde.sim.core.simulation.time.TimeLapseHandle;

/**
 * @author Rinde van Lon (rinde.vanlon@cs.kuleuven.be)
 * @author dmerckx
 */
public class TimeLapseTest {
    
	@Test
	public void constructor() {
	    TimeIntervalImpl master = new TimeIntervalImpl(0, 10);
		TimeLapse tl = new TimeLapseHandle(master);

		assertEquals(0, tl.getCurrentTime());
		assertEquals(0, tl.getTimeConsumed());
		assertEquals(10, tl.getTimeStep());
		assertEquals(10, tl.getTimeLeft());
		assertTrue(tl.hasTimeLeft());

	}

	@SuppressWarnings("unused")
	@Test(expected = IllegalArgumentException.class)
	public void constructorFail1() {
        new TimeIntervalImpl(-1, 10);
	}

	@SuppressWarnings("unused")
	@Test(expected = IllegalArgumentException.class)
	public void constructorFail2() {
	    new TimeIntervalImpl(0, 0);
	}

	@Test
	public void consume1(){
	    TimeIntervalImpl master = new TimeIntervalImpl(0, 10);
	    
	    //View that controller/guards have
	    TimeLapseHandle handle = new TimeLapseHandle(master); 
	    //View that users have
	    TimeLapse tl = handle;
	    
        assertEquals(0, tl.getStartTime());
        assertEquals(10, tl.getEndTime());
        assertEquals(0, tl.getCurrentTime());
        assertEquals(10, tl.getTimeLeft());
	    
        tl.consume(6);
        
        assertEquals(6, tl.getTimeConsumed());
        assertEquals(0, tl.getStartTime());
        assertEquals(10, tl.getEndTime());
        assertEquals(6, tl.getCurrentTime());
        assertEquals(4, tl.getTimeLeft());

        master.nextStep();

        assertEquals(10, tl.getStartTime());
        assertEquals(20, tl.getEndTime());
        assertEquals(10, tl.getCurrentTime());
        assertEquals(10, tl.getTimeLeft());
        
        tl.consume(6);

        assertEquals(10, tl.getStartTime());
        assertEquals(20, tl.getEndTime());
        assertEquals(16, tl.getCurrentTime());
        assertEquals(4, tl.getTimeLeft());
	}
    
    @Test
    public void consumeLessThenOneStep(){
        int[] startL = { 0, 18, 137, 1200 };
        int[] stepL = { 100, 9, 93, 70 };
        int[] consumeL = { 50, 4, 45, 70 };

        for (int i = 0; i < startL.length; i++) {
            int start = startL[i];
            int step = stepL[i];
            int consume = consumeL[i];
            

            TimeIntervalImpl master = new TimeIntervalImpl(start, step);
            TimeLapseHandle handle = new TimeLapseHandle(master); 
            TimeLapse tl = handle;
            assertEquals(start, tl.getStartTime());
            assertEquals(start+step, tl.getEndTime());
            assertEquals(start, tl.getCurrentTime());
            assertEquals(step, tl.getTimeLeft());
            
            tl.consume(consume);
            
            assertTrue(tl.getTimeLeft() == 0 || tl.hasTimeLeft());
            assertEquals(consume, tl.getTimeConsumed());
            assertEquals(start, tl.getStartTime());
            assertEquals(start+step, tl.getEndTime());
            assertEquals(start+consume, tl.getCurrentTime());
            assertEquals(step-consume, tl.getTimeLeft());
        }
    }
    
    /**
     * When more then one step is consumed this means that the timelapse
     * will be turns thereafter as well, until the fully consumed amount has
     * passed.
     */
    @Test
    public void consumeMoreThenOneStep(){
        int[] startL = { 0, 18, 1500, 137 };
        int[] stepL =   { 100, 9, 930, 93 };
        int[] consumeL = { 100, 13, 1050, 2*93 };

        //Note that this test is only for: step <= consumption <= 2*step
        for (int i = 0; i < startL.length; i++) {
            int start = startL[i];
            int step = stepL[i];
            int consume = consumeL[i];
            

            TimeIntervalImpl master = new TimeIntervalImpl(start, step);
            TimeLapseHandle handle = new TimeLapseHandle(master);
            
            TimeLapse tl = handle;
            assertEquals(start, tl.getStartTime());
            assertEquals(start+step, tl.getEndTime());
            assertEquals(start, tl.getCurrentTime());
            assertEquals(step, tl.getTimeLeft());
            
            tl.consume(consume);
            
            assertFalse(tl.hasTimeLeft());
            assertEquals(step, tl.getTimeConsumed());
            assertEquals(start, tl.getStartTime());
            assertEquals(start+step, tl.getEndTime());
            assertEquals(start+step, tl.getCurrentTime());
            assertEquals(0, tl.getTimeLeft());
            
            master.nextStep();
            
            int left = consume - step;
            
            assertEquals(left, tl.getTimeConsumed());
            assertEquals(start+step, tl.getStartTime());
            assertEquals(start+2*step, tl.getEndTime());
            assertEquals(start+step+left, tl.getCurrentTime());
            assertEquals(step - left, tl.getTimeLeft());
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void negativeConsumption() {
        TimeIntervalImpl master = new TimeIntervalImpl(0, 10);
        TimeLapse tl = new TimeLapseHandle(master);
        tl.consume(-1);
    }
    
    /**
     * We cannot consume time unless the time left is > 0.
     */
    @Test(expected = IllegalArgumentException.class)
    public void consumptionWhenTimeLeftIsZero() {
        TimeIntervalImpl master = new TimeIntervalImpl(0, 10);
        TimeLapse tl = new TimeLapseHandle(master);
        tl.consume(10);
        tl.consume(10);
    }
    
    /**
     * A TimeLapseHandle can be locked, meaning it will never be given
     * any time unless it is explicitly unlocked.
     */
    @Test
    public void lockTimeLapse() {
        TimeIntervalImpl master = new TimeIntervalImpl(0, 10);
        TimeLapseHandle handle = new TimeLapseHandle(master);
        handle.consume(4);
        assertEquals(6, handle.getTimeLeft());
        
        handle.block();
        //As soon as a handle is blocked any remaining time is consumed
        assertEquals(0, handle.getTimeLeft());
        
        //Indefinitely..
        for(int i = 0; i < 100; i++){
            master.nextStep();
            assertEquals(0, handle.getTimeLeft());
        }
        
        //Until it is unblocked
        handle.unblock();
        assertEquals(10, handle.getTimeLeft());
        
        
        //But if we proceed to the next turn..
        master.nextStep();
        //Time is available again
        assertEquals(10, handle.getTimeLeft());
    }
    
    @Test
    public void lockTimeLapse2() {
        TimeIntervalImpl master = new TimeIntervalImpl(0, 10);
        TimeLapseHandle handle = new TimeLapseHandle(master);
        handle.consume(4);
        assertEquals(6, handle.getTimeLeft());
        
        handle.block();
        assertEquals(0, handle.getTimeLeft());
        
        //We can unblock with a given amount, this amount will
        //consumed as soon as time is available again
        handle.unblock(15);
        assertEquals(0, handle.getTimeLeft());
        
        master.nextStep();
        //10 time is available but we consumed an additional 15,
        //so nothing is available yet
        assertEquals(5, handle.getTimeLeft());
    }

}
