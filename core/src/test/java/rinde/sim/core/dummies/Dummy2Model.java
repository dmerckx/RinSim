package rinde.sim.core.dummies;

import java.util.List;

import rinde.sim.core.simulation.UserInit;

public class Dummy2Model extends DummyAbstrModel<Dummy2Data, Dummy2User>{

    @Override
    public List<UserInit<?>> register(Dummy2User user, Dummy2Data data) {
        assert(data != null);
        
        System.out.println("register " + user + " " + data);
        
        user.initData = data;
        
        return super.register(user, data);
    }
    
    @Override
    public Class<Dummy2User> getSupportedType() {
        return Dummy2User.class;
    }

}
