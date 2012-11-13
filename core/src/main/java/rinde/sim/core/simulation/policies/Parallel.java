package rinde.sim.core.simulation.policies;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import rinde.sim.core.simulation.Simulator;
import rinde.sim.core.simulation.TickListener;
import rinde.sim.core.simulation.TickPolicy;
import rinde.sim.core.simulation.TimeInterval;

/**
 * A parallel implementation of a general policy. In general no guarantees are
 * made in what order listeners will be executed, any amount of listeners could
 * be executed simultaneously (in parallel).
 * 
 * However the following mechanism is available if a deterministic actions is required:
 * During execution the listeners are scheduled in a certain order, a listener executing
 * his {@link TickListener tick} method can call {@link ThreadState awaitAllPrevious},
 * this will cause the thread to wait until all listeners that were scheduled
 * before him are done executing.
 * Calling {@link ThreadState awaitAllPrevious} will have a small performance cost,
 * this should only be called in cases were this is unavoidable.
 * For example, calling {@link ThreadState awaitAllPrevious} at the start of the 
 * {@link TickListener tick} of each listener will simply result in sequential execution
 * (with extra overhead). 
 * 
 * @author dmerckx
 *
 * @param <T> The type of tick listeners accepted by this policy.
 * @param <I> The type of time accepted tick listeners are using.
 */
public abstract class Parallel<T extends TickListener<I>, I extends TimeInterval> extends ThreadState implements TickPolicy<T>{
    
    private boolean register;
    private Class<T> acceptedClass;
    
    /**
     * The listeners of this policy, stored in the order they
     * were registered.
     */
    protected Set<T> listeners = new LinkedHashSet<T>();
    
    /**
     * Calls 
     * @param targetClass
     */
    public Parallel(Class<T> targetClass) {
        this(false, targetClass);
    }
    
    public Parallel(boolean register, Class<T> acceptedClass) {
        this.register = register;
        this.acceptedClass = acceptedClass;
    }
    
    @Override
    public Class<T> getAcceptedType() {
        return acceptedClass;
    }
    
    @Override
    public void register(T listener) {
        listeners.add(listener);
    }
    
    @Override
    public void unregister(T listener) {
        listeners.remove(listener);
    }

    @Override
    public void performTicks(final TimeInterval interval) {
        List<Future<?>> futures = new ArrayList<Future<?>>();
        List<CountDownLatch> barriers = new ArrayList<CountDownLatch>();
        
        for(final T listener:listeners){
            final ThreadLocal<CountDownLatch> local = previousBarrier;
            final CountDownLatch barrier = new CountDownLatch(1);
            barriers.add(barrier);
            futures.add(pool.submit(new Runnable() {
                @Override
                public void run() {
                    local.set(barrier);
                    listener.tick(getI(interval));
                    local.set(null);
                }
            }));
        }
        
        for(int i = 0; i < futures.size(); i++){
            try {
                futures.get(i).get();
            } catch (Exception e) {
                e.printStackTrace();
            }
            barriers.get(i).countDown();
        }
    }
    
    /**
     * @return Returns a certain time instance for executing {@link TickListener tick}.
     * @param interval The interval for which to generate (a more specific) time instance
     */
    protected abstract I getI(TimeInterval interval);
    
    @Override
    public boolean canRegisterDuringExecution() {
        return register;
    }

    @Override
    public boolean canUnregisterDuringExecution() {
        return register;
    }
}
