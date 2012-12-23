package rinde.sim.core.model.interaction;

import java.io.Serializable;
import java.util.List;

import rinde.sim.core.graph.Point;
import rinde.sim.core.model.interaction.users.InteractionUser;
import rinde.sim.core.simulation.TimeLapse;

/**
 * A visitor used by an {@link InteractionUser}.
 * 
 * Ones used this visitor will be passed a list of all the {@link Receiver}s
 * in his particular position. 
 * 
 * The visitor can access all the public methods available on the {@link Receiver}s.
 * After this interaction the result should be formulated as a kind of {@link Result}.
 * 
 * It is this result that is eventually returned to the {@link InteractionUser}
 * that used this visitor.
 * 
 * Note: 
 * A visitor is typically made stateless after its initialization. It makes usually
 * makes little sense to keep any kind of state in between 2 visits.
 * 
 * @author dmerckx
 *
 * @param <T> The type of receivers to which this visitor applies.
 * @param <R> The type of result this visitor will generate.
 */
@SuppressWarnings("serial")
public abstract class Visitor<T extends Receiver, R extends Result> implements Serializable{

    /**
     * The location at which this visitor is active.
     */
    public final Point location;
    
    /**
     * The type of {@link Receiver}s that this visitor can handle. 
     */
    public final Class<T> target; 
    
    /**
     * Create a new visitor.
     * @param target The type of receivers that will be targeted.
     * @param location The location of this visitor.
     */
    @SuppressWarnings("hiding")
    public Visitor(Class<T> target, Point location) {
        this.target = target;
        this.location = location;
    }
    
    /**
     * Perform the actual visit. A list of receivers is given which
     * can all be accessed. 
     * 
     * During this visit it is guaranteed that no one else can access
     * the provided receivers.
     * 
     * @param lapse The timelapse at which this visit takes place.
     * @param receivers The receivers 
     * @return The result of this visit.
     */
    public abstract R visit(TimeLapse lapse, List<T> receivers);
}
