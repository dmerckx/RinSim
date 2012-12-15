package rinde.sim.core.simulation;

import rinde.sim.core.model.Data;
import rinde.sim.core.model.User;

public interface SimulatorToModelAPI {

    public void registerUser(User<Data> user);
    
    public <D extends Data> void registerUser(User<D> user, D data);
    
    public <U extends User<?>> U getApi(User<?> user, Class<U> type);
}
