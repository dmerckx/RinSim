package rinde.sim.core.model.road;

import java.io.Serializable;
import java.util.List;

import rinde.sim.core.model.road.users.RoadUser;
import rinde.sim.core.simulation.time.TimeLapse;

public interface Visitor<T extends RoadUser, R extends Serializable> {

    public R visit(List<T> targets, TimeLapse time);
}
