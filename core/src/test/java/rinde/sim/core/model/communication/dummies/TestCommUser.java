package rinde.sim.core.model.communication.dummies;

import rinde.sim.core.graph.Point;
import rinde.sim.core.model.communication.apis.CommAPI;
import rinde.sim.core.model.communication.users.CommUser;
import rinde.sim.core.model.road.apis.RoadAPI;


public class TestCommUser implements CommUser{

    public final TestCommUnit unit;

    public RoadAPI roadAPI;
    public CommAPI commAPI;
    
    public TestCommUser(double rad) {
        this(new Point(0,0), rad, 1.0);
    }

    public TestCommUser(Point pos, double rad, double rel) {
        this.unit = new TestCommUnit(this, pos, rad, rel);
    }

    @Override
    public TestCommUnit buildUnit() {
        return unit;
    }

}
