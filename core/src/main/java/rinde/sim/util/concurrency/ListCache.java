package rinde.sim.util.concurrency;

import java.util.List;

import rinde.sim.core.model.pdp.Parcel;
import rinde.sim.core.simulation.TimeInterval;

import com.google.common.collect.ImmutableList;

public class ListCache<T> {

    private final TimeInterval globalTime;
    
    private ImmutableList<T> actualValue;
    private ImmutableList<T> backupValue;
    private long lastChangedTime;
    
    public ListCache(TimeInterval globalTime, T... initValues) {
        this.globalTime = globalTime;
        
        this.actualValue = ImmutableList.copyOf(initValues);
        this.backupValue = actualValue;
        
        this.lastChangedTime = globalTime.getStartTime();
    }

    
    public synchronized void addValue(T value){
        if(globalTime.getStartTime() > lastChangedTime){
            backupValue = actualValue;
            lastChangedTime = globalTime.getStartTime();
        }
        actualValue = new ImmutableList.Builder<T>().
                        addAll(actualValue).
                        add(value).
                        build();
    }
    
    public synchronized void removeValue(T value){
        if(globalTime.getStartTime() > lastChangedTime){
            backupValue = actualValue;
            lastChangedTime = globalTime.getStartTime();
        }

        ImmutableList.Builder<T> builder = new ImmutableList.Builder<T>();
        for(T val:actualValue){
            if(!val.equals(value))
                builder.add(val);
        }
        actualValue = builder.build();
    }
    
    public ImmutableList<T> getActualValue(){
        return actualValue;
    }
    
    public synchronized ImmutableList<T> getFrozenValue(){
        if(globalTime.getStartTime() == lastChangedTime)
            //The value was changed during this turn, use backup
            return backupValue;
        else
            //The value was (not yet) changed during this turn, use actual
            return actualValue;
    }
}