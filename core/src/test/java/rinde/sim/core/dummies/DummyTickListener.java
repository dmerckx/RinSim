package rinde.sim.core.dummies;

import rinde.sim.core.simulation.TickListener;
import rinde.sim.core.simulation.TimeInterval;

public class DummyTickListener implements TickListener {
    private int count = 0;

    public int getTickCount() {
        return count;
    }
    
    @Override
    public void tick(TimeInterval time) {
        count++;
    }
}
