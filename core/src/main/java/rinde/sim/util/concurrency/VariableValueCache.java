package rinde.sim.util.concurrency;

import rinde.sim.core.simulation.TimeInterval;

public class VariableValueCache<T> implements ValueCache<T>{
    private final TimeInterval globalTime;
    
    private T actualValue;
    
    volatile private long lastChangedTime;
    volatile private T frozenValue;
    
    public VariableValueCache(T initValue, TimeInterval globalTime) {
        this.globalTime = globalTime;
        this.actualValue = initValue;
        this.frozenValue = initValue;
        this.lastChangedTime = globalTime.getStartTime();
    }

    
    public synchronized void setValue(T value){
        if(globalTime.getStartTime() > lastChangedTime){
            update();
        }
        actualValue = value;
    }
    
    public T getActualValue(){
        return actualValue;
    }
    
    public T getFrozenValue(){
        if(globalTime.getStartTime() == lastChangedTime)
            //The value was changed during this turn, use backup
            return frozenValue;
        
        synchronized (this) {
            if(globalTime.getStartTime() == lastChangedTime)
                return frozenValue;
            
            update();
            
            return frozenValue;
        }
    }
    
    private void update(){
        frozenValue = actualValue;
        lastChangedTime = globalTime.getStartTime();
    }
}