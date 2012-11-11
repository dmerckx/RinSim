package rinde.sim.core.refs;

public final class ConstantRef<T> implements Ref<T>, RefBackup<T> {

    private final T value;
    
    public ConstantRef(T value) {
        this.value = value;
    }
    
    @Override
    public T getValue() {
        return value;
    }
    
    @Override
    public T getLastValue() {
        return value;
    }

    @Override
    public void addListener(UpdateListener listener) {}

    @Override
    public void removeListener(UpdateListener listener) {}
    
}
