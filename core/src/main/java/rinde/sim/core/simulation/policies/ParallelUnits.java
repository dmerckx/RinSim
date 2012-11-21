package rinde.sim.core.simulation.policies;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;

import rinde.sim.core.model.Unit;
import rinde.sim.core.simulation.TickPolicy;
import rinde.sim.core.simulation.TimeInterval;
import rinde.sim.core.simulation.TimeLapse;
import rinde.sim.core.simulation.time.TimeLapseGroup;

public class ParallelUnits extends ThreadState implements TickPolicy<Unit>{

    private LinkedHashSet<Unit> units = new LinkedHashSet<Unit>();
    final TimeLapseGroup group = new TimeLapseGroup();
    
    @Override
    public Class<Unit> getAcceptedType() {
        return Unit.class;
    }

    @Override
    public void register(Unit listener) {
        units.add(listener);
    }

    @Override
    public void unregister(Unit listener) {
        units.remove(listener);
    }

    @Override
    public void performTicks(final TimeInterval interval) {
        List<Future<?>> futures = new ArrayList<Future<?>>();
        List<CountDownLatch> barriers = new ArrayList<CountDownLatch>();
        
        for(final Unit unit:units){
            final ThreadLocal<CountDownLatch> local = previousBarrier;
            final CountDownLatch barrier = new CountDownLatch(1);
            barriers.add(barrier);
            
            futures.add(pool.submit(new Runnable() {
                @Override
                public void run() {
                    local.set(barrier);
                    TimeLapse lapse = group.forge(interval);
                    
                    unit.tick(lapse);
                    
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
        
        for(final Unit unit:units){
            futures.add(pool.submit(new Runnable() {
                @Override
                public void run() {
                    unit.afterTick(interval);
                }
            }));
        }
    }

    @Override
    public boolean canRegisterDuringExecution() {
        return false;
    }

    @Override
    public boolean canUnregisterDuringExecution() {
        return false;
    }

}
