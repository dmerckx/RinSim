package rinde.sim.util.positions;

public interface Filter<T> {
    
    /**
     * Return true if value should be filtered out of the results
     */
    boolean matches(T t);
}
