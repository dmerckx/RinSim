package rinde.sim.core.simulation.policies;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import rinde.sim.core.simulation.Simulator;

/**
 * @author dmerckx
 *
 */
public class ParallelExecution {
    
    /**
     * This bounds the maximal number of threads used by all running instances of
     * {@link Parallel} (also across different running {@link Simulator}s).
     */
    public static final int NR_THREADS = 8;
    protected static ExecutorService pool = Executors.newFixedThreadPool(NR_THREADS);
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
