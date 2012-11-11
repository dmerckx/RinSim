package rinde.sim.core.simulation;

public interface TickListener<T extends TimeInterval> {

    public void tick(T time);
}
