package rinde.sim.util;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import rinde.sim.core.graph.Point;
import rinde.sim.util.positions.Filter;
import rinde.sim.util.positions.PositionCache;
import rinde.sim.util.positions.Region;

public class PositionCacheTest {

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {}

    @Before
    public void setUp() throws Exception {}

    @Test
    public void testRegion() {
        PositionCache<Object> cache = new PositionCache<Object>(0, 1, 0, 1, 10);
        
        assertEquals(new Region(2, 2), cache.getRegion(new Point(0.2, 0.2)));
        assertEquals(new Region(3, 2), cache.getRegion(new Point(0.32, 0.2)));
        assertEquals(new Region(7, 8), cache.getRegion(new Point(0.77, 0.81)));
    }
    
    @Test
    public void testPosition() {
        for(int seed:new int[]{19,71,83,123,156,255}){
            PositionCache<Object> cache = new PositionCache<Object>(0, 1, 0, 1, 100);
            Random rand = new Random(seed);
            
            List<Value> initialData = new ArrayList<Value>();
            
            for(int i = 0; i < 1000; i++){
                Point pos = new Point(rand.nextDouble(), rand.nextDouble());
                Object obj = new Object();
                Value val = new Value(pos, obj); 
                
                initialData.add(val);
                cache.add(pos, obj);
            }
            
            //A random position that will be checked
            Point pos = new Point(rand.nextDouble(), rand.nextDouble());
            
            Object closestObjectCache = cache.getClosestTo(pos, new DummyFilter());
            
            Value closestValue = initialData.get(0);
            for(Value val:initialData){
                if(Point.distance(val.pos, pos) < Point.distance(closestValue.pos, pos)){
                    closestValue = val;
                }
            }
            
            assertEquals(closestValue.val, closestObjectCache);
        }
    }
    
    @Test
    public void bulkTest() {
        PositionCache<Object> cache = new PositionCache<Object>(0, 1, 0, 1, 100);
        Random rand = new Random(79);
        
        List<Value> initialData = new ArrayList<Value>();
        
        for(int i = 0; i < 5000; i++){
            Point pos = new Point(rand.nextDouble(), rand.nextDouble());
            Object obj = new Object();
            Value val = new Value(pos, obj); 
            
            initialData.add(val);
            cache.add(pos, obj);
        }
        
        List<Point> testPoints = new ArrayList<Point>();
        for(int j = 0; j < 20000; j++){
            testPoints.add(new Point(rand.nextDouble(), rand.nextDouble()));
        }
        
        List<Object> correspondingValuesFast = new ArrayList<Object>();
        long start = System.currentTimeMillis();
        for(Point p:testPoints){
            correspondingValuesFast.add( cache.getClosestTo(p, new DummyFilter()));
        }
        long timeFast = System.currentTimeMillis() - start;
        

        List<Object> correspondingValuesSlow = new ArrayList<Object>();
        start = System.currentTimeMillis();
        for(Point p:testPoints){
            Value closestValue = initialData.get(0);
            for(Value val:initialData){
                if(Point.distance(val.pos, p) < Point.distance(closestValue.pos, p)){
                    closestValue = val;
                }
            }
            correspondingValuesSlow.add(closestValue.val);
        }
        long timeSlow = System.currentTimeMillis() - start;
        
        for(int i = 0; i < testPoints.size(); i++){
            assertEquals(correspondingValuesSlow.get(i), correspondingValuesFast.get(i));
        }
        
        System.out.println("Fast: " + timeFast);
        System.out.println("Slow: " + timeSlow);
    }
}

class DummyFilter implements Filter<Object>{
    @Override
    public boolean matches(Object t) {
        return false;
    }
    
}

class Value {
    public final Point pos;
    public final Object val;

    @SuppressWarnings("hiding") 
    Value(Point pos, Object val){
        this.pos = pos;
        this.val = val;
    }
    
    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof Value)) return false;
        
        return val.equals(((Value) obj).val);
    }
}