package rinde.sim.core.simulation.policies.agents.util;



public class WarmupTask implements Runnable{
    //Store the result in a public variable, so it is less likely to be optimized away
    public String dummy;   
    
    @Override
    public void run() {
        for(int i = 0; i < 1000; i++){
            dummy += i + "j*";
        }
    }
}