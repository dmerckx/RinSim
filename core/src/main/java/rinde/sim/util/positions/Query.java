package rinde.sim.util.positions;

public interface Query<T> {
    
    /**
     * Return true if value should be filtered out of the results
     */
    boolean process(T t);
    
    Class<T> getType();
}
