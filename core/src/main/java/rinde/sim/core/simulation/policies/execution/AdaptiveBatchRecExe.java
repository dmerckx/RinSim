package rinde.sim.core.simulation.policies.execution;

import java.util.List;

import rinde.sim.core.simulation.policies.agents.AgentContainer;
import rinde.sim.core.simulation.policies.agents.util.LatchNode;

public class AdaptiveBatchRecExe extends BatchRecExe{

    /*protected static int[] BATCHSIZES =
            //new int[]{1, 2, 4, 8, 16, 32, 64, 128, 200, 300, 400, 500, 550, 600};
            new int[]{1, 2, 4, 8, 16, 32, 64, 80, 110, 130, 150, 180, 220, 250};
    {
        BATCHSIZES = new int[200];
        
        for(int i = 0; i < 200; i++){
            BATCHSIZES[i] = i * 10 + 1;
        }
    }*/
    
    
    int b = 0;
    int prevB = 0;
    long time = 0;
    long prevTime = 0;
    
    int stickyTime = 0;
    int firstTrans = 0;
    
    public AdaptiveBatchRecExe(){
        super(1);
    }
    
    public void setTime(long newTime){
        prevTime = time;
        time = newTime;
    }

    @Override
    public LatchNode execute(LatchNode startNode,
            List<AgentContainer> containers) {
        int bBackup = b;
        
        if(b == 0){
            stickyTime--;
            if(stickyTime == 0){
                firstTrans++;
                stickyTime = firstTrans * firstTrans;
                b = 1;
            }
        }
        else if(prevTime < time) b = prevB; //revert to last size
        else b += b - prevB; //continue the same trend
        
        if(getBatchSize(b) > (containers.size() / pool.cores)){
            b -= 2;
            //System.out.println("   ~> ");
        }
        
        prevB = bBackup;
        batchSize = getBatchSize(b);
        
        //System.out.println("pre: " + prevTime + "now: " + time + " -> " + batchSize);
        if(batchSize == 1)
            return executeSingle(startNode, containers);
        return super.execute(startNode, containers);
    }
    
    private LatchNode executeSingle(LatchNode startNode, List<AgentContainer> agents) {
        LatchNode lastNode = startNode;
        
        //The main thread start by dividing the work in pieces
        for(AgentContainer c:agents){
            pool.addTask(new RealSingleTask(c, lastNode, rules));
            lastNode = lastNode.makeNext();
        }
        
        return lastNode;
    }
    
    private int getBatchSize(int b){
        return b * b + 1;
    }
}
