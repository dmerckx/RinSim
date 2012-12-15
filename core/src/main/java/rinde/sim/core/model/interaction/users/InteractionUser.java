package rinde.sim.core.model.interaction.users;

import rinde.sim.core.model.Data;
import rinde.sim.core.model.User;
import rinde.sim.core.model.interaction.apis.InteractionAPI;

public interface InteractionUser<D extends Data> extends User<D> {

    public void setInteractionAPi(InteractionAPI api);
}
