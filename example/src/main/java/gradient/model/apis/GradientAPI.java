package gradient.model.apis;

import rinde.sim.core.graph.Point;
import rinde.sim.core.model.road.apis.RoadAPI;

public interface GradientAPI {
    
    void init(RoadAPI roadAPI);
    
    /**
     * Searches in 9 directions and picks the best target.
     * @param How far in each direction the algorithm should look.
     * @return The best target direction to travel to.
     */
    Point getTarget(double distance);
    
    /**
     * Returns the value of the field in the given location for this emitter.
     * @param The location to calculate the field at.
     * @return The value of the field.
     */
    double getField(Point p);
    
    /**
     * Toggles emitting a field on/off.
     * @param Whether or not the field should be active.
     */
    void setIsActive(boolean active);
 
    /**
     * Returns if the field is active.
     * @return Whether or not the fields is active.
     */
    boolean getCurrentIsActive();

	GradientState getState();
}
