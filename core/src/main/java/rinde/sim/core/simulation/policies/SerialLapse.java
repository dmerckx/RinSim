package rinde.sim.core.simulation.policies;

import rinde.sim.core.simulation.TickListener;
import rinde.sim.core.simulation.TimeInterval;
import rinde.sim.core.simulation.TimeLapse;
import rinde.sim.core.simulation.time.TimeLapseImpl;

public class SerialLapse<T extends TickListener<TimeLapse>> extends Serial<T>{

    public SerialLapse(boolean register, Class<T> acceptedClass) {
        super(register, acceptedClass);
    }

    private TimeLapseImpl lapse = new TimeLapseImpl();
    
    @Override
    public void performTicks(TimeInterval interval) {
        for(T listener:listeners){
            listener.tick(lapse.initialize(interval.getStartTime(), interval.getEndTime()));
        }
    }
    
}