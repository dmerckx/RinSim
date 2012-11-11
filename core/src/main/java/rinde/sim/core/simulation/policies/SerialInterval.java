package rinde.sim.core.simulation.policies;

import rinde.sim.core.simulation.TickListener;
import rinde.sim.core.simulation.TimeInterval;

/**
 * The serial implementation specifically for tickListeners requiring a {@link TimeInterval}
 * for execution.
 * 
 * @author dmerckx
 * @param <T> The type of tick listeners accepted by this policy.
 */
public class SerialInterval<T extends TickListener<TimeInterval>> extends Serial<T>{

    
    @SuppressWarnings("javadoc")
    public SerialInterval(boolean register, Class<T> acceptedClass) {
        super(register, acceptedClass);
    }

    @Override
    public void performTicks(TimeInterval interval) {
        for(T listener:listeners){
            listener.tick(interval);
        }
    }
}
