package rinde.sim.util.concurrency;

import rinde.sim.core.simulation.TimeInterval;

public class ValueCache<T> {

    private final TimeInterval globalTime;
    
    private T actualValue;
    private T backupValue;
    private long lastChangedTime;
    
    public ValueCache(T initValue, TimeInterval globalTime) {
        this.globalTime = globalTime;
        this.actualValue = initValue;
        this.backupValue = initValue;
        this.lastChangedTime = globalTime.getStartTime();
    }

    
    public /*synchronized*/ void setValue(T value){
        if(globalTime.getStartTime() > lastChangedTime){
            backupValue = actualValue;
            lastChangedTime = globalTime.getStartTime();
        }
        actualValue = value;
    }
    
    public T getActualValue(){
        return actualValue;
    }
    
    public /*synchronized*/ T getFrozenValue(){
        if(globalTime.getStartTime() == lastChangedTime)
            //The value was changed during this turn, use backup
            return backupValue;
        else
            //The value was (not yet) changed during this turn, use actual
            return actualValue;
    }
}
