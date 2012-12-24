package rinde.sim.core.model.road.users;


/**
 * Initialization data for {@link MovingRoadUser}s.
 * 
 * @author dmerckx
 */
public interface MovingRoadData extends RoadData{

    /**
     * The initial maximum speed to be used.
     * @return The initial max speed.
     */
    public double getInitialSpeed();
}
