package rinde.sim.core.simulation.time;

import java.util.HashMap;
import java.util.Map;

import rinde.sim.core.simulation.TimeInterval;
import rinde.sim.core.simulation.TimeLapse;



public class TimeLapseGroup {

    private final Map<Long,TimeLapseImpl> lapses;
    
    public TimeLapseGroup() {
        lapses = new HashMap<Long, TimeLapseImpl>();
    }
    
    public TimeLapse forge(TimeInterval interval) {
        long threadID = Thread.currentThread().getId();
        
        if(!lapses.containsKey(threadID)){
            //No need to handle concurrent modification since
            //threadID is always unique for each thread
            lapses.put(threadID, new TimeLapseImpl());
        }
        
        return lapses.get(threadID).initialize(interval.getStartTime(), interval.getEndTime());
    }
}
