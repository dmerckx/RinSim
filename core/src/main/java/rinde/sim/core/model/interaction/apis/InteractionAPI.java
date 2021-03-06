package rinde.sim.core.model.interaction.apis;

import java.util.List;

import rinde.sim.core.model.communication.Message;
import rinde.sim.core.model.interaction.Receiver;
import rinde.sim.core.model.interaction.Result;
import rinde.sim.core.model.interaction.Visitor;
import rinde.sim.core.simulation.TimeLapse;

/**
 * The API provided to interaction users, every interaction user
 * will get his own personal API assigned.
 * 
 * This API allows users to perform interactions with each other.
 * There are 2 interactors for each action:
 *  - {@link Receiver}:
 *      Interaction users can advertise a receiver. A receiver
 *      will passively wait until a visitor comes by to interact.
 *      After an interaction a receiver can decide to send a 
 *      notification to its creating user and/or terminate itself.
 *      The creating user can at any point choose to terminate
 *      or replace the receiver itself.
 *  - {@link Visitor}:
 *      Interaction users can use a visitor to visit a certain location.
 *      All receivers stationed at this location will be visited and
 *      interactions can take place here. The result build up during
 *      these visits will eventually be returned.
 * 
 * @author dmerckx
 */
public interface InteractionAPI{
    /**
     * Use a visitor, it will be applied to all the receivers
     * on its location.
     * @param lapse The timelapse required to perform this operation.
     * @param visitor The visitor to be applied.
     * @return The result applying this visitor.
     */
    public <R extends Result> R visit(TimeLapse lapse, Visitor<?, R> visitor);
    
    /**
     * Advertise a receiver, it will wait on its specified location for
     * visitors to come by.
     * 
     * Note that this receiver should not share any state with this agent,
     * it should not have any references to objects which share state with
     * any agents. 
     * If this requirement is not met determinism can not be ensured.
     * 
     * @param receiver The receiver to advertise.
     */
    public void advertise(Receiver receiver);
    
    /**
     * Stop advertising any receiver.
     */
    public void stopAdvertising();

    /**
     * Returns whether or not this API is advertising a receiver.
     * @return True iff this user is advertising a receiver.
     */
    public boolean isAdvertising();
}
