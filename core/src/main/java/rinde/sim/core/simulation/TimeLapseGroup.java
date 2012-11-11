package rinde.sim.core.simulation;

import java.util.HashMap;
import java.util.Map;


public class TimeLapseGroup {

    private final Map<Long,TimeLapse> lapses;
    
    public TimeLapseGroup() {
        lapses = new HashMap<Long, TimeLapse>();
    }
    
    public TimeLapse forge(TimeInterval interval) {
        long threadID = Thread.currentThread().getId();
        
        if(!lapses.containsKey(threadID)){
            //No need to handle concurrent modification since
            //threadID is always unique for each thread
            lapses.put(threadID, new TimeLapse());
        }
        
        return lapses.get(threadID).initialize(interval.startTime, interval.endTime);
    }
}
