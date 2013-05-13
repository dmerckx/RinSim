package rinde.sim.util.positions;

import java.util.List;

import rinde.sim.core.graph.Point;
import rinde.sim.core.model.road.users.RoadUser;
import rinde.sim.util.Rectangle;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

@SuppressWarnings({"javadoc", "hiding"})
public class PositionCache<T extends RoadUser<?>> {
    private final double xMin;
    private final double yMin;
    private final double width;
    private final double height;
    
    private final int nrBlocks;
    
    private Multimap<Region, T> map;
    private List<Update<T>> updates;
     
    public PositionCache(Rectangle bounds, int nrBlocks) {
        this.xMin = bounds.xMin;
        this.yMin = bounds.yMin;
        this.width = bounds.xMax - bounds.xMin;
        this.height = bounds.yMax - bounds.yMin;
        
        this.nrBlocks = nrBlocks;
        
        map = LinkedHashMultimap.create();
        updates = Lists.newArrayList();
    }
    
    public void add(T obj){
        Point pos = obj.getRoadState().getLocation();
        map.put(getRegion(pos), obj);
    }
    
    public void remove(T obj){
        Point pos = obj.getRoadState().getLocation();
        map.remove(getRegion(pos), obj);
    }
    
    public void update(T obj, Point newPos){
        Region from = getRegion(obj.getRoadState().getLocation());
        Region to = getRegion(newPos);
        if(!from.equals(to)){
            synchronized (this) {
                updates.add(new Update<T>(from, to, obj));
            }
        }
    }
    
    public <T2 extends T> void query(Point pos, double range, Query<T2> query){
        Region minReg = getRegion(new Point(pos.x - range, pos.y - range));
        Region maxReg = getRegion(new Point(pos.x + range, pos.y + range));
       
        for(Region reg:getNeighbours(minReg, maxReg)){
            executeQueryIn(pos, range, reg, query);
        }
    }
    
    protected <T2 extends T> void executeQueryIn(Point pos, double range, Region reg, Query<T2> q){
        for(T obj:map.get(reg)){
            if(!q.getType().isInstance(obj)) return;
            
            if(Point.distance(obj.getRoadState().getLocation(), pos) < range){
                q.process((T2) obj);
            } 
        }
    }
    
    /**
     * Get all the neighboring regions that are located within
     * a certain range of amount of blocks.
     * @param pos The position from where to start searching.
     * @param range The range within to search (in blocks).
     * @return The neighboring regions.
     */
    protected List<Region> getNeighbours(Region minReg, Region maxReg){
        List<Region> results = Lists.newArrayList();
        
        for(int x = minReg.x; x <= maxReg.x; x++){
            for(int y = minReg.y; y <= maxReg.y; y++){
                results.add(new Region(x, y));
            }
        }
        
        return results;
    }
    
    /**
     * Return the region in which a position is located.
     * @param pos The position.
     * @return The region in which the position is located.
     */
    public Region getRegion(Point pos){
        //Casting to int will floor the result
        int x = (int) (scaleX(pos.x) * nrBlocks);  
        int y = (int) (scaleY(pos.y) * nrBlocks);  
        
        if( x == nrBlocks ) x = nrBlocks - 1;
        if( y == nrBlocks ) y = nrBlocks - 1;
        
        assert x >= 0 && x < nrBlocks;
        assert y >= 0 && y < nrBlocks;
        
        return new Region(x, y);
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
    
    @Override
    public String toString() {
        return map.toString();
    }
}

class Update<T>{
    public final Region from;
    public final Region to;
    public final T obj;
    
    public Update(Region from, Region to, T obj) {
        this.from = from;
        this.to = to;
        this.obj = obj;
    }
}