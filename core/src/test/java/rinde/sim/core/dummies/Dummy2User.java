package rinde.sim.core.dummies;

import rinde.sim.core.model.User;

public class Dummy2User implements User<Dummy2Data>{

    public int nr;
    
    public Dummy2User() {
        this(0);
    }
    
    public Dummy2User(int nr) {
        this.nr = nr;
    }
    
    public Dummy2Data initData = null;
 
    @Override
    public String toString() {
        return super.toString() + "|" + nr;
    }
}
