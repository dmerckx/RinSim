package rinde.sim.util.concurrency;


public interface ValueCache<T> {
    
    public void setValue(T value);
    
    public T getActualValue();
    
    public T getFrozenValue();
}
