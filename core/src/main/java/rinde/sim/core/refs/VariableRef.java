package rinde.sim.core.refs;

import java.util.ArrayList;
import java.util.List;

public class VariableRef<T> implements Ref<T> {
    
    private T value;
    private List<UpdateListener> listeners;
    
    public VariableRef(T value) {
        this.value = value;
        this.listeners = new ArrayList<UpdateListener>();
    }
    
    public T getValue(){
        return value;
    }
    
    public void updateValue(T value){
        this.value = value;
        
        for(UpdateListener l: listeners){
            l.notifyUpdate();
        }
    }

    public void addListener(UpdateListener listener) {
        listeners.add(listener);
    }
    
    public void removeListener(UpdateListener listener){
        listeners.remove(listener);
    }
}
