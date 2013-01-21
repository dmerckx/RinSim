package rinde.sim.core.model.road.users;

import rinde.sim.core.graph.Point;


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
    
    
    public static class Std implements MovingRoadData{
        private final Point startPos;
        private final double speed;
        
        public Std(Point startPos, double speed){
            this.startPos = startPos;
            this.speed = speed;
        }
        
        @Override
        public Point getStartPosition() {
            return startPos;
        }

        @Override
        public double getInitialSpeed() {
            return speed;
        }
        
    }
}
