package rinde.sim.util.concurrency;

import rinde.sim.core.simulation.TimeInterval;
import rinde.sim.core.simulation.time.TimeLapseHandle;

public abstract class StateCache<T> {

    private final TimeInterval globalTime;
    
    protected long lastChangedTime;
    protected long duration;
    
    private long lastUpdatedTime;
    
    private T actualState;
    private T frozenState;
    
    public StateCache(T initValue, TimeInterval globalTime) {
        this.globalTime = globalTime;
        
        this.actualState = initValue;
        this.frozenState = initValue;
        
        this.lastChangedTime = globalTime.getStartTime();
        this.lastUpdatedTime = -1;
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
        if(globalTime.getStartTime() == lastUpdatedTime) update();
        return actualState;
    }
    
    public synchronized T getFrozenValue(){
        if(globalTime.getStartTime() == lastChangedTime)
            //The value was changed during this turn, use backup
            return frozenState;
        else if(globalTime.getStartTime() == lastUpdatedTime)
            //
            return actualState;
        else
            //
            update();
            return actualState;
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
        
        lastUpdatedTime = globalTime.getStartTime();
    }
    
    public abstract T getState(T currentState, long time);
}
