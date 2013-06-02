package rinde.sim.util.positions;

import java.util.List;

import rinde.sim.core.graph.Point;
import rinde.sim.core.model.road.users.RoadUser;
import rinde.sim.core.simulation.TimeInterval;
import rinde.sim.util.Rectangle;

import com.google.common.collect.Lists;

@SuppressWarnings({"javadoc", "hiding"})
public class ConcurrentPositionCache<T extends RoadUser<?>> {
    private final double xMin;
    private final double yMin;
    private final double width;
    private final double height;
    
    private final TimeInterval clock;

    public final int nrBlocks;
    public final ConcurrentRegion<T>[] regions;
    
    public ConcurrentPositionCache(Rectangle bounds, int nrBlocks, TimeInterval clock) {
        if (clock == null) throw new IllegalStateException();
        
        this.xMin = bounds.xMin;
        this.yMin = bounds.yMin;
        this.width = bounds.xMax - bounds.xMin;
        this.height = bounds.yMax - bounds.yMin;
        
        this.nrBlocks = nrBlocks;
        this.clock = clock;
        
        this.regions = new ConcurrentRegion[nrBlocks * nrBlocks];
    }
    
    public void add(T obj){
        Point pos = obj.getRoadState().getLocation();
        getRegion(pos).directAdd(obj);
    }
    
    public void remove(T obj){
        Point pos = obj.getRoadState().getLocation();
        getRegion(pos).directRemove(obj);
    }
    
    public void update(T obj, Point oldPos, Point newPos){
        if(getRegion(oldPos) != getRegion(newPos)){
            getRegion(oldPos).removeUser(obj);
            getRegion(newPos).addUser(obj);
        }
    }
    
    public void query(Point pos, double range, Query query){
        Region minReg = getRegion(new Point(pos.x - range, pos.y - range));
        Region maxReg = getRegion(new Point(pos.x + range, pos.y + range));
       
        for(ConcurrentRegion<T> reg:getNeighbours(minReg, maxReg)){
            reg.queryUpon(pos, range, query);
        }
    }
    
    /**
     * Get all the neighboring regions that are located within
     * a certain range of amount of blocks.
     * @param pos The position from where to start searching.
     * @param range The range within to search (in blocks).
     * @return The neighboring regions.
     */
    protected List<ConcurrentRegion<T>> getNeighbours(Region minReg, Region maxReg){
        List<ConcurrentRegion<T>> results = Lists.newArrayList();
        
        for(int x = minReg.x; x <= maxReg.x; x++){
            for(int y = minReg.y; y <= maxReg.y; y++){
                results.add(getRegion(x,y));
            }
        }
        
        return results;
    }
    
    public ConcurrentRegion<T> getRegion(Point pos){
        //Casting to int will floor the result
        int x = (int) (scaleX(pos.x) * nrBlocks);  
        int y = (int) (scaleY(pos.y) * nrBlocks);  
        
        return getRegion(x, y);
    }
    
    /**
     * Return the region in which a position is located.
     * @param pos The position.
     * @return The region in which the position is located.
     */
    public ConcurrentRegion<T> getRegion(int x, int y){
        if( x == nrBlocks ) x = nrBlocks - 1;
        if( y == nrBlocks ) y = nrBlocks - 1;
        
        assert x >= 0 && x < nrBlocks;
        assert y >= 0 && y < nrBlocks;
        
        if(regions[x * nrBlocks + y] != null)
            return regions[x * nrBlocks + y];
     
        synchronized(regions){
            if(regions[x * nrBlocks + y] == null)
                regions[x * nrBlocks + y] = new ConcurrentRegion<T>(x, y, clock);

            return regions[x * nrBlocks + y];
        }
    }
    
    /**
     * Returns a scaled x value between 0 and 1.
     */
    protected double scaleX(double x){
        if(x < xMin) return 0;
        if(x > xMin + width) return 1;
        return (x - xMin) / width;
    }

    /**
     * Returns a scaled y value between 0 and 1.
     */
    protected double scaleY(double y){
        if(y < yMin) return 0;
        if(y > yMin + height) return 1;
        return (y - yMin) / height;
    }
}