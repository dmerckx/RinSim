package rinde.sim.core.model.interaction.users;

import rinde.sim.core.model.Data;
import rinde.sim.core.model.User;
import rinde.sim.core.model.interaction.apis.InteractionAPI;
import rinde.sim.core.simulation.Simulator;

/**
 * Represents a interaction user, able to advertise receivers and visit
 * certain locations with a visitor. 
 * Whenever an interaction user is registered in the {@link Simulator}
 * it will be assigned a {@link InteractionAPI}.
 * This API will be set via the {@link InteractionAPI setInteractionAPI}
 * method, and can be accessed thereafter to use receivers and visitors.
 * 
 * @author dmerckx
 *
 * @param <D> The type of initialization data.
 */
public interface InteractionUser<D extends Data> extends User<D> {

    /**
     * Sets the interaction API of this user.
     * 
     * Note:
     * This method should simply store the given API.
     * No side effects should be applied during this call.
     * 
     * @param api The interaction API.
     */
    public void setInteractionAPi(InteractionAPI api);
}
