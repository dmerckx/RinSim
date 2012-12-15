package rinde.sim.core.model;

import rinde.sim.core.simulation.TimeInterval;

public interface AfterTickGuard extends TimeUser{

    public void afterTick(TimeInterval interval);
}
