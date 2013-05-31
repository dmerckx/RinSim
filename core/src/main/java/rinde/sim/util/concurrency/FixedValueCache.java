package rinde.sim.util.concurrency;

public class FixedValueCache<T> implements ValueCache<T>{

    private final T value;
    
    public FixedValueCache(T value) {
        this.value = value;
    }
    
    @Override
    public void setValue(T value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public T getActualValue() {
        return value;
    }

    @Override
    public T getFrozenValue() {
        return value;
    }
}
