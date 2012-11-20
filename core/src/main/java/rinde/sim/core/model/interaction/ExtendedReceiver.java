package rinde.sim.core.model.interaction;

import rinde.sim.core.graph.Point;
import rinde.sim.core.model.interaction.guards.InteractiveGuard;

public abstract class ExtendedReceiver<N extends Notification> extends Receiver{

    private final InteractiveGuard<? super N> guard;
    
    /**
     * @param location The location of this receiver.
     * @param guard The guard from which this receiver originates.
     */
    public ExtendedReceiver(Point location, InteractiveGuard<? super N> guard) {
        super(location);
        this.guard = guard;
    }
    
    public final void sendNotification(N notification){
        guard.receiveNotification(notification);
    }
}
