package rinde.sim.core.model.pdp.users;

import rinde.sim.core.graph.Point;
import rinde.sim.core.model.pdp.Parcel;
import rinde.sim.core.model.road.users.MovingRoadData;

public interface TruckData extends ContainerData, MovingRoadData{

    /**
     * A standard implementation available for easy use.
     * @author dmerckx
     */
    public static class Std implements TruckData{
        private final double speed;
        private final Point position;
        private final double capacity;
        
        public Std(double speed, Point pos, double cap){
            this.speed = speed;
            this.position = pos;
            this.capacity = cap;
        }
        
        @Override
        public double getCapacity() {
            return capacity;
        }

        @Override
        public Class<? extends Parcel> getParcelType() {
            return Parcel.class;
        }

        @Override
        public Point getStartPosition() {
            return position;
        }

        @Override
        public double getInitialSpeed() {
            return speed;
        }
        
    }
}
