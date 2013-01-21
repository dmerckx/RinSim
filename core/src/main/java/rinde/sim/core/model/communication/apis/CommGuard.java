package rinde.sim.core.model.communication.apis;

import rinde.sim.core.graph.Point;
import rinde.sim.core.model.communication.CommunicationModel;
import rinde.sim.core.model.communication.Delivery;
import rinde.sim.core.model.communication.Message;
import rinde.sim.core.model.communication.users.CommData;
import rinde.sim.core.model.communication.users.FullCommUser;
import rinde.sim.core.simulation.time.TimeLapseHandle;

/**
 * An implementation of the {@link CommAPI}.
 * 
 * This guard expands {@link SimpleCommGuard} by also providing means
 * to handle the broadcasting of messages.
 * 
 * @author dmerckx
 */
public class CommGuard extends SimpleCommGuard implements CommAPI{

    private double radius;
    private final FullCommUser<?> user;

    /**
     * Construct a new guard. 
     * @param user The user to which this API belongs.
     * @param data The initialization data for this API.
     * @param model The communication model.
     * @param seed The seed used for generating random number.
     */
    @SuppressWarnings("hiding")
    public CommGuard(FullCommUser<?> user, CommData data, CommunicationModel model, long seed, TimeLapseHandle handle) {
        super(user, data, model, seed, handle);
        this.user = user;
        
        this.radius = data.getInitialRadius();
    }
    
    /**
     * Returns the location of this guard, at the start of this tick.
     * @return The location at the start of this tick.
     */
    public Point getLastLocation(){
        return user.getRoadState().getLocation();
        
    }
    
    // ----- COMM API ----- //

    @Override
    public double getRadius(){
        return radius;
    }
    
    @SuppressWarnings("hiding")
    @Override
    public void setRadius(double radius) {
        this.radius = radius;
    }

    @Override
    public void broadcast(Message message) {
        if(rnd.nextFloat() <= reliability)
            model.broadcast(new Delivery(address, message));
    }
}
