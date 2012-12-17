package rinde.sim.core.model;


import rinde.sim.core.simulation.Simulator;
import rinde.sim.core.simulation.TickListener;
import rinde.sim.core.simulation.TimeLapse;

/**
 * This extension of the general {@link TickListener} is used to
 * model agents in the simulator. An agent will typically also be
 * registered onto a few {@link Model}s.
 * 
 * Every tick of the {@link Simulator} the {@link Agent tick} method
 * is called during which the agent is able to reason.
 * Actions can be performed on the the {@link Model}s on which the agent
 * is registered. Some of these actions will consume time. This is modeled
 * by a {@link TimeLapse}. A fresh {@link TimeLapse} is received each tick
 * and can be passed to the appropriate actions.
 * 
 * @author dmerckx
 */
public interface Agent extends TimeUser{
    
    /**
     * An agent can 'use' the provided time to perform actions.
     * Actions are methods that specify an operation (usually on a
     * {@link Model}) that takes time. The {@link TimeLapse} reference that
     * is received through this method can be used to spent on these time
     * consuming actions.
     */
    public void tick(TimeLapse time);
}
