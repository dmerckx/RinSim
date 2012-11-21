package rinde.sim.core.model.interaction;

import rinde.sim.core.graph.Point;
import rinde.sim.core.model.interaction.guards.InteractiveGuard;

public abstract class ExtendedReceiver extends Receiver{

    private InteractiveGuard guard;
    private boolean set = false;
    
    /**
     * @param location The location of this receiver.
     * @param guard The guard from which this receiver originates.
     */
    public ExtendedReceiver(Point location) {
        super(location);
    }
    
    public final void sendNotification(Notification notification){
        guard.receiveNotification(notification);
    }
    
    public final void setGuard(InteractiveGuard guard){
        assert(!set);
        this.guard = guard;
        set = true;
    }
}
