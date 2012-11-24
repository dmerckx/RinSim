package rinde.sim.core.model;

import rinde.sim.core.simulation.TimeInterval;
import rinde.sim.core.simulation.TimeLapse;

public interface Unit{
    
    public User getElement();
    
    public void init();
    
    public void registerForTick(PreTick preTicker);
    
    public void registerAfterTick(AfterTick afterTicker);
    
    public void tick(TimeLapse lapse);
    
    public void afterTick(TimeInterval time);
    
    public interface PreTick{
        public void tick(TimeLapse lapse);
    }
    
    public interface AfterTick{
        public void afterTick(TimeInterval time);
        
    }
}
