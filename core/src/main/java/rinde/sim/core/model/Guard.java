package rinde.sim.core.model;

import rinde.sim.core.simulation.TickListener;
import rinde.sim.core.simulation.TimeInterval;
import rinde.sim.core.simulation.types.Agent;

/**
 * This extension of the general {@link TickListener} is used to
 * model helper classes of {@link Model}s in the simulator.
 * An agent will typically not communicate directly with a {@link Model}
 * but trough an interface provided by this {@link Model}, the
 * implementation of this interface can be a {@link Guard}.
 * 
 * Ports are designed to represent agent in their respective {@link Model}s.
 * Ports should be completely transparent for users that are not interested
 * in creating custom models. Beside serving as a proxy between {@link Agent}
 * and {@link Model} these classes often handle parts of the synchronization
 * for parallel execution and can store {@link Agent} specific state (that
 * they are not allowed to access directly).
 * 
 * @author dmerckx
 */
public interface Guard{
    
}
