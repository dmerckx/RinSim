package rinde.sim.util.concurrency;

import rinde.sim.core.simulation.TimeInterval;
import rinde.sim.core.simulation.time.TimeLapseHandle;

public abstract class StateCache<T> {

    private final TimeInterval globalTime;
    
    protected long duration;
    private T actualState;
    
    volatile protected long lastChangedTime;
    volatile private T frozenState;
    
    public StateCache(T initValue, TimeInterval globalTime) {
        this.globalTime = globalTime;
        
        this.actualState = initValue;
        this.frozenState = initValue;
        
        this.lastChangedTime = globalTime.getStartTime();
    }

    public synchronized void setValue(T value){
        setValue(value, 0);
    }
    
    public synchronized void setValue(T value, long duration){
        if(globalTime.getStartTime() > lastChangedTime){
            frozenState = actualState;
            lastChangedTime = globalTime.getStartTime();
        }
        this.duration = globalTime.getStartTime() + duration;
        
        update();
        actualState = value;
        update();
    }
    
    public synchronized T getActualValue(){
        if(globalTime.getStartTime() != lastChangedTime) update();
        return actualState;
    }
    
    public T getFrozenValue(){
        if(globalTime.getStartTime() == lastChangedTime)
            //The value was changed during this turn, use backup
            return frozenState;
        
        synchronized (this) {
            if(globalTime.getStartTime() == lastChangedTime)
                return frozenState;
            
            update();
            return actualState;
        }
    }
    
    private void update(){
        T lastState = null;
        while(actualState != lastState){
            lastState = actualState;
            actualState = getState(lastState, globalTime.getStartTime());
        }
        if(globalTime.getStartTime() > lastChangedTime){
            frozenState = actualState;
            lastChangedTime = globalTime.getStartTime();
        }
        duration = 0;
    }
    
    public abstract T getState(T currentState, long time);
}
