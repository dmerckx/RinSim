package rinde.sim.core.model.interaction;

import java.util.List;

import rinde.sim.core.graph.Point;
import rinde.sim.core.simulation.TimeLapse;

public abstract class Visitor<T extends ExtendedReceiver, R extends Result> {

    public final Point location;
    public final Class<T> target; 
    
    /**
     * @param target The type of receivers that will be targeted.
     * @param location The location of this visitor.
     */
    @SuppressWarnings("hiding")
    public Visitor(Class<T> target, Point location) {
        this.target = target;
        this.location = location;
    }
    
    public abstract R visit(TimeLapse lapse, List<T> receivers);
}
