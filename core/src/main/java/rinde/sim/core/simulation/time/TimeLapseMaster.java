package rinde.sim.core.simulation.time;

public class TimeLapseMaster {

    private long time;
    private long step;
    
    public TimeLapseMaster(long step) {
        this.time = 0;
        this.step = step;
    }
    
    public void nextStep(){
        time += step;
    }
    
    public long getTime(){
        return time;
    }
}
