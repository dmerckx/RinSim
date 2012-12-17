package rinde.sim.core.dummies;

import rinde.sim.core.model.Agent;
import rinde.sim.core.simulation.TimeLapse;

public class DummyUserAgent extends DummyUser implements Agent{

    public int ticks = 0;
    
    @Override
    public void tick(TimeLapse time) {
        ticks++;
    }

}
