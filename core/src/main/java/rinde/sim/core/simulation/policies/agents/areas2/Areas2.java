package rinde.sim.core.simulation.policies.agents.areas2;

import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;

import rinde.sim.core.graph.Point;
import rinde.sim.core.model.Agent;
import rinde.sim.core.model.road.RoadModel;
import rinde.sim.core.model.road.apis.MovingRoadGuard;
import rinde.sim.core.model.road.apis.RoadGuard;
import rinde.sim.core.model.road.users.RoadUser;
import rinde.sim.core.simulation.TimeInterval;
import rinde.sim.core.simulation.policies.InteractionRules;
import rinde.sim.core.simulation.policies.agents.AgentsPolicyAbstr;
import rinde.sim.core.simulation.policies.agents.util.LatchNode;
import rinde.sim.core.simulation.time.TimeLapseHandle;
import rinde.sim.util.Rectangle;

import com.google.common.collect.Lists;

public class Areas2 extends AgentsPolicyAbstr{
    
    protected int batchSize;
    
    protected final AreaRules rules = createRules();
    
    protected final AreaWorker[][] workers;
    protected final LinkedBlockingQueue<Runnable>[] tasks;
    protected final int nrRegions;
    protected final int threadsPerRegio;
    
    protected Rectangle mapsize;
    protected double width;
    
    public Areas2(int batchSize, int nrRegions, int threadsPerRegio) {
        this.batchSize = batchSize;
        this.nrRegions = nrRegions;
        
        workers = new AreaWorker[nrRegions][threadsPerRegio];
        tasks = new LinkedBlockingQueue[nrRegions];
        
        this.threadsPerRegio = threadsPerRegio;
        
        for(int r = 0; r < nrRegions; r++){
            tasks[r] = new LinkedBlockingQueue<Runnable>();
            
            for(int t = 0; t < threadsPerRegio; t++){
                workers[r][t] = createWorker(r);
                workers[r][t].start();
            }
        }
    }
    
    protected AreaRules createRules(){
        return new AreaRules(this);
    }
    
    protected AreaWorker createWorker(int region){
        return new AreaWorker(tasks[region], rules);
    }
    
    protected Runnable makeTask(List<Entry<Agent,TimeLapseHandle>> batch, int region){
        return new Task2(batch, region, rules);
    }

    @Override
    public void doTicks(TimeInterval interval) {
        //System.out.println("doTicks");
        CountDownLatch stuckCounter = new CountDownLatch(nrRegions * threadsPerRegio);
        
        LatchNode masterNode = new LatchNode();
        LatchNode nextNode = masterNode.makeNext();
        for(int r = 0; r < nrRegions; r++){
            LatchNode stuckNode = new LatchNode();
            for(int t = 0; t < threadsPerRegio; t++){
                workers[r][t].activate (stuckNode, nextNode, stuckCounter);
                nextNode = nextNode.makeNext();
                stuckNode = stuckNode.makeNext();
            }
        }
        
        Iterator<Entry<Agent, TimeLapseHandle>> it = agents.entrySet().iterator();
        
        List<Entry<Agent,TimeLapseHandle>>[] batches = new List[nrRegions];
        
        for(int i = 0; i < batches.length; i++){
            batches[i] = Lists.newArrayListWithCapacity(batchSize);
        }
        
        //The main thread start by dividing the work in pieces
        while(it.hasNext()){
            Entry<Agent, TimeLapseHandle> entry = it.next();
            
            Point pos = ((MovingRoadGuard)((RoadUser<?>) entry.getKey()).getRoadState()).getTarget();     
            int region = getRegion(pos);
            
            batches[region].add(entry);
            
            if(batches[region].size() % nrRegions == 0){
                try {
                    tasks[region].put(makeTask(batches[region], region));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                batches[region] = Lists.newArrayList();
            }
        }

        for(int region = 0; region < nrRegions; region++){
            if(!batches[region].isEmpty()){
                try {
                    tasks[region].put(makeTask(batches[region], region));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                batches[region] = Lists.newArrayList();
            }
        }
        
        //System.out.println("All tasks inserted");

        for(int r = 0; r < nrRegions; r++){
            for(int t = 0; t < threadsPerRegio; t++){
                workers[r][t].allTasksInserted();
            }
        }
        
        //System.out.println("wait for stuck counter");
       
        /*do{
            //System.out.println(stuckCounter.getCount());
            //System.out.println(tasks[0].size());
        }while(stuckCounter.getCount() != 0);*/
        
        try {
            //Thread.sleep(1000);
            ////System.out.println(stuckCounter.getCount());
            stuckCounter.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        //System.out.println("All threads are stuck");
        
        //At this point all threads are stuck
        
        // ---Step 1---
        // Finish the rest of the work sequentially
        
        rules.task.set(null);
        rules.worker.set(null);
        for(int region = 0; region < nrRegions; region++){
            while(!tasks[region].isEmpty()){
                //System.out.println("Region " + region + " still has " + tasks[region].size() + " tasks");
                try {
                    tasks[region].take().run();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        // ---Step 2---
        // Sequentially deblock all the threads
        
        //System.out.println("Now deblock all threads one by one"); 
        
        masterNode.done();
        
        try {
            nextNode.awaitPrevious();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public int getRegion(Point pos){
        return (int) Math.floor((pos.x - mapsize.xMin) / width * nrRegions) % nrRegions;
    }
    
    @Override
    public InteractionRules getInteractionRules() {
        return rules;
    }

    @Override
    public void warmUp() {
        
        
    }

    @Override
    public void shutDown() {
        for(int r = 0; r < nrRegions; r++){
            for(int t = 0; t < threadsPerRegio; t++){
                workers[r][t].close();
            }
        }
    }

    public void init(Rectangle mapSize) {
        super.init(mapSize);
        
        this.initUsers.clear();
        this.agents.clear();
        
        this.mapsize = mapSize;
        this.width = mapsize.xMax - mapsize.xMin;
    }
}

class Task2 implements Runnable{
    private final List<Entry<Agent, TimeLapseHandle>> batch;
    private final AreaRules rules;
    
    public final int region;
    
    private Agent currentAgent;
    private AreaWorker worker;
    
    public Task2(List<Entry<Agent,TimeLapseHandle>> batch, int region, AreaRules rules) {
        this.batch = batch;
        this.region = region;
        this.rules = rules;
    }
    
    public RoadUser<?> getCurrentAgenRoadUser(){
        return (RoadUser<?>) currentAgent;
    }
    
    public void run(){
        for(Entry<Agent,TimeLapseHandle> e:batch){
            currentAgent = e.getKey();
            e.getKey().tick(e.getValue());
        }
    }
}

class AreaRules implements InteractionRules {
    private final Areas2 areas;
    public final ThreadLocal<Task2> task = new ThreadLocal<Task2>();
    public final ThreadLocal<AreaWorker> worker = new ThreadLocal<AreaWorker>();
    
    public AreaRules(Areas2 areas) {
        this.areas = areas;
    }
    
    @Override
    public void awaitAllPrevious() {
        Task2 t = task.get();
        
        if(t == null) return;
        
        if(areas.getRegion(((RoadGuard) t.getCurrentAgenRoadUser().getRoadState()).getCurrentLocation()) != t.region){
            //block this thread
            //System.out.println("Stuck on " + t);
            //System.out.println("t: " + t);
            worker.get().stuck();
        }
    }

    @Override
    public boolean isDeterministic() {
        return true;
    }

    @Override
    public void notifyQuery(double range) {
        // TODO Auto-generated method stub
        
    }
}

class AreaWorker extends Thread {
    private final LinkedBlockingQueue<Runnable> tasks;
    private final AreaRules rules;
    private CountDownLatch startupLatch;
    
    private LatchNode stuckNode;
    private LatchNode sequentialNode;
    private CountDownLatch stuckCounter;
    
    public AreaWorker(LinkedBlockingQueue<Runnable> tasks, AreaRules rules) {
        this.tasks = tasks;
        this.rules = rules;
        this.startupLatch = new CountDownLatch(1);
        setDaemon(true);
    }
    
    public void activate(LatchNode stuckNode, LatchNode sequentialNode, CountDownLatch stuckCounter) {
        this.stuckNode = stuckNode;
        this.sequentialNode = sequentialNode;
        
        this.stuckCounter = stuckCounter;
        
        this.allTasksInserted = false;
        this.startupLatch.countDown();
    }
    
    public void stuck(){
        stuckNode.done();
        stuckCounter.countDown();
        try {
            sequentialNode.awaitPrevious();
        } catch (InterruptedException e) {
            // Should never happen
            e.printStackTrace();
        }
    }
    
    private boolean allTasksInserted;
    public synchronized void allTasksInserted(){
        allTasksInserted = true;
    }
    
    private boolean running = true;
    public void close(){
        running = false;
        this.interrupt();
    }

    @Override
    public void run() {
        while(running){  
            try {
                this.startupLatch.await();
            } catch (InterruptedException e2) {}
            
            //A new turn has begun
            
            try {
                //Await the previous thread to be stuck
                stuckNode.awaitPrevious();
            } catch (InterruptedException e1) {
                continue;
            }
            
            //Previous turn is stuck, my time to process tasks!
            while(true){
                if(allTasksInserted && tasks.isEmpty()){
                    stuck();
                    
                    //Prepare evrything for the next turn
                    this.startupLatch = new CountDownLatch(1);
                    
                    //End this turn
                    sequentialNode.done();
                    
                    break;
                }
                else if(tasks.isEmpty()){
                    Thread.yield();
                    //Try again
                    continue;
                }
                
                try {
                    Runnable task = tasks.take();
                    
                    rules.worker.set(this);
                    if(task instanceof Task2){
                        rules.task.set(((Task2) task));
                    }
                    
                    task.run();
                } catch (InterruptedException e) {
                    break;
                } 
            }
        }
        /*while(running){    //When interrupted restart from here
            try {
                stuckNode.awaitPrevious();
            } catch (InterruptedException e1) {
                continue;
            }
            
            while(true){
                if(allTasksInserted && tasks.isEmpty()){
                    stuck();
                    sequentialNode.done();
                    break;
                }
                else if(tasks.isEmpty()){
                    Thread.yield();
                    //Try again
                    continue;
                }
                
                try {
                    Runnable task = tasks.take();
                    
                    rules.worker.set(this);
                    if(task instanceof Task2){
                        rules.task.set(((Task2) task));
                    }
                    
                    task.run();
                } catch (InterruptedException e) {
                    break;
                } 
            }
        }*/
    }
}