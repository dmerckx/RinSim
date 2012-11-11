package rinde.sim.core.refs;


public interface Ref<T>{

    public T getValue();

    public void addListener(UpdateListener listener);
    
    public void removeListener(UpdateListener listener);
}
