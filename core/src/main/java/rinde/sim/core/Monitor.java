package rinde.sim.core;

public class Monitor {
    static private Monitor inst;
    
    public static final Monitor get(){
        if(inst == null) inst = new Monitor();
        return inst;
    }
    
    //Time used by models
    private long models = 0;
    
    //Time used to simulate agents
    private long agents = 0;
        //  -part used to increase lapses
    private long lapses = 0;
    
    
    private long startModels;
    private long startAgents;
    private long startLapses;
    
    public void startModels(){
        startModels = System.nanoTime();
    }
    
    public void endModels(){
        models += System.nanoTime() - startModels;
    }
    
    public void startAgents(){
        startAgents = System.nanoTime();
    }
    
    public void endAgents(){
        agents += System.nanoTime() - startAgents;
    }
    
    public void startLapses(){
        startLapses = System.nanoTime();
    }
    
    public void endLapses(){
        lapses += System.nanoTime() - startLapses;
    }

    public void printReport(){
        long total = models + agents;
        
        System.out.println("" + models +  " " + agents + " " + lapses);
        
        double modelsTime = (1.0d * models)/total;
        double agentsTime = (1.0d * agents)/total;
        double lapsesTime = (1.0d * lapses)/agents;
        double restTime = 1-lapsesTime;
        
        System.out.println("------");
        System.out.println("Models time: " + modelsTime);
        System.out.println("Agents time: " + agentsTime);
        System.out.println("  ->lapses:  " + lapsesTime);
        System.out.println("  ->rest:    " + restTime);
        System.out.println("------");
    }
}
