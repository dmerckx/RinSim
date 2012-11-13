package rinde.sim.core.simulation.types;

import rinde.sim.core.model.Model;
import rinde.sim.core.simulation.TickListener;

/**
 * This extension of the general {@link TickListener} is used to
 * model helper classes of {@link Model}s in the simulator.
 * An agent will typically not communicate directly with a {@link Model}
 * but trough an interface provided by this {@link Model}, the
 * implementation of this interface can be a {@link AgentPort}.
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
public interface AgentPort extends Agent{
    
    public Agent getAgent();
}
