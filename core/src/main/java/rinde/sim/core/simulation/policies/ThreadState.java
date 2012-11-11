package rinde.sim.core.simulation.policies;

import java.util.concurrent.CountDownLatch;

/**
 * @author dmerckx
 *
 */
public class ThreadState {

    protected static ThreadLocal<CountDownLatch> previousBarrier = new ThreadLocal<CountDownLatch>();

    public static void awaitAllPrevious(){
        if( previousBarrier.get() == null)
            return;

        try {
            previousBarrier.get().await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

}
