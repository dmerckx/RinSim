package rinde.sim.util.positions;

import java.util.List;

import rinde.sim.core.graph.Point;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

@SuppressWarnings({"javadoc", "hiding"})
public class PositionCache<T> {
    
    private final double xMin;
    private final double yMin;
    private final double width;
    private final double height;
    
    private final int nrBlocks;
    
    private Multimap<Region, Value<T>> map;

    public PositionCache(double xMin, double xMax, double yMin, double yMax) {
        //50 is a decently good setup for setups with >250 agents
        this(xMin, xMax, yMin, yMax, 50);
    }
    
    public PositionCache(double xMin, double xMax, double yMin, double yMax, int nrBlocks) {
        this.xMin = xMin;
        this.yMin = yMin;
        this.width = xMax - xMin;
        this.height = yMax - yMin;
        
        this.nrBlocks = nrBlocks;
        
        map = ArrayListMultimap.create();
    }
    
    public void add(Point pos, T obj){
        map.put(getRegion(pos), new Value<T>(pos, obj));
        
        //System.out.println(map);
    }
    
    public void remove(Point pos, T obj){
        map.remove(getRegion(pos), new Value<T>(pos, obj));
    }
    
    public T getClosestTo(Point pos, Filter<T> filter){
        if(map.size() < 250) return getClosestBrute(pos, filter);
        
        Region orig = getRegion(pos);
        
        //Try to find at least a valid point that is less then 1 block away
        Result<T> bestResult = search(orig, pos, filter, 1, 0);
        if(bestResult != null) return bestResult.val;
        
        //Try to find at least a valid point that is less then 3 blocks away
        bestResult = search(orig, pos, filter, 2, 0);
        if(bestResult != null) return bestResult.val;

        //Try to find at least a valid point that is less then 3 blocks away
        bestResult = search(orig, pos, filter, 3, 0);
        if(bestResult != null) return bestResult.val;

        //Try to find at least a valid point that is less then 3 blocks away
        bestResult = search(orig, pos, filter, 4, 1);
        if(bestResult != null) return bestResult.val;
        
        //Try to find at least a valid point that is less then 5 blocks away
        bestResult = search(orig, pos, filter, 5, 1);
        if(bestResult != null) return bestResult.val;
        
        //Try to find at least a valid point that is less then 5 blocks away
        bestResult = search(orig, pos, filter, 6, 2);
        if(bestResult != null) return bestResult.val;
        
        //Try to find at least a valid point that is less then 8 blocks away
        bestResult = search(orig, pos, filter, 8, 3);
        if(bestResult != null) return bestResult.val;
        
        //Try to find at least a valid point that is less then 8 blocks away
        bestResult = search(orig, pos, filter, 9, 3);
        if(bestResult != null) return bestResult.val;
        
        //Give up and just check everything
        return getClosestBrute(pos, filter);
    }
    
    protected T getClosestBrute(Point pos, Filter<T> filter){
        double minDist = Double.MIN_VALUE;
        T result = null;
        
        for(Value<T> v:map.values()){
            if(filter.matches(v.val))continue;
            
            if(result == null || Point.distance(v.pos, pos) < minDist){
                minDist = Point.distance(v.pos, pos);
                result = v.val;
            }
        }
        
        return result;
    }
    
    public Result<T> search(Region orig, Point pos, Filter<T> filter, int range, int excludedRange){
        Result<T> bestResult = null;
        
        for(Region reg:getNeighbours(orig, range, excludedRange)){
            Result<T> res = getClosestIn(reg, pos, filter, ((range * width) / nrBlocks));
            if(res == null) continue;
            if(bestResult == null || res.dist < bestResult.dist){
                bestResult = res;
            }
        }
        
        return bestResult;
    }
    
    protected Result<T> getClosestIn(Region reg, Point pos, Filter<T> filter, double maxRange){
        double minDist = maxRange;
        T resultValue = null;
        
        for(Value<T> val:map.get(reg)){
            if(filter.matches(val.val)) continue;
            
            if(Point.distance(val.pos, pos) < minDist){
                minDist = Point.distance(val.pos, pos);
                resultValue = val.val;
            }
        }
        
        return resultValue == null ? null : new Result<T>(minDist, resultValue);
    }
    
    /**
     * Get all the neighboring regions that are located within
     * a certain range of amount of blocks.
     * @param pos The position from where to start searching.
     * @param range The range within to search (in blocks).
     * @return The neighboring regions.
     */
    protected List<Region> getNeighbours(Region reg, int range, int excludeRange){
        List<Region> results = Lists.newArrayList();
        
        for(int x = reg.x - range; x <= reg.x + range; x++){
            for(int y = reg.y - range; y <= reg.y + range; y++){
                if(x >= reg.x - excludeRange && x <= reg.x + excludeRange
                   && y >= reg.y - excludeRange && y <= reg.y + excludeRange
                        && excludeRange != 0) continue;
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
        //Casting to long will floor the result
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
        return (x - xMin) / width;
    }

    /**
     * Returns a scaled y value between 0 and 1.
     */
    protected double scaleY(double y){
        return (y - yMin) / height;
    }
    
    @Override
    public String toString() {
        return map.toString();
    }
}

class Result<T>{
    public final double dist;
    public final T val;

    @SuppressWarnings("hiding") 
    Result(double dist, T val){
        this.dist = dist;
        this.val = val;
    }
}

class Value<T> {
    public final Point pos;
    public final T val;

    @SuppressWarnings("hiding") 
    Value(Point pos, T val){
        this.pos = pos;
        this.val = val;
    }
    
    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof Value)) return false;
        
        return val.equals(((Value<?>) obj).val);
    }
    
    @Override
    public String toString() {
        return "{Val:" + pos + "}";
    }
}