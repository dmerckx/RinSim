package rinde.sim.util;

import rinde.sim.core.graph.Point;

public class Rectangle{
    public final double xMin;
    public final double xMax;
    public final double yMin;
    public final double yMax;
    
    public Rectangle(double xMin, double xMax, double yMin, double yMax) {
        this.xMin = xMin;
        this.xMax = xMax;
        this.yMin = yMin;
        this.yMax = yMax;
    }
    
    public boolean isOutsideBounds(Point pos){
        return pos.x < xMin || pos.x > xMax
                || pos.y < yMin || pos.y > yMax;
    }
    
    @Override
    public String toString() {
        return "from: " + xMin + "," + yMin + " to " + xMax + "," + yMax;
    }
}
