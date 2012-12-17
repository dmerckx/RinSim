package rinde.sim.core.simulation;

import rinde.sim.core.model.Data;
import rinde.sim.core.model.User;

public class UserInit<D extends Data> {

    public static <D extends Data> UserInit<D> create(User<D> user, D data){
        return new UserInit<D>(user, data);
    }
    
    public static UserInit<Data> create(User<Data> user){
        return new UserInit<Data>(user, new Data() {});
    }
    
    public final User<D> user;
    public final D data;
    
    private UserInit(User<D> user, D data){
        this.user = user;
        this.data = data;
    }
}
