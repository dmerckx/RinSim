package rinde.sim.core.model;

import rinde.sim.core.simulation.TickListener;
import rinde.sim.core.simulation.TimeInterval;
import rinde.sim.core.simulation.TimeLapse;

public interface Holder extends TickListener<TimeLapse>{
    
    public Object getElement();
    
    @Override
    public void tick(TimeLapse time);
    
    public void afterTick(TimeInterval time);
}
