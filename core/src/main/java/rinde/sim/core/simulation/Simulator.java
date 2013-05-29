package rinde.sim.core.simulation;

import java.util.List;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

import rinde.sim.core.Monitor;
import rinde.sim.core.model.Agent;
import rinde.sim.core.model.Data;
import rinde.sim.core.model.InitUser;
import rinde.sim.core.model.Model;
import rinde.sim.core.model.ModelManager;
import rinde.sim.core.model.ModelProvider;
import rinde.sim.core.model.User;
import rinde.sim.core.model.road.RoadModel;
import rinde.sim.core.simulation.policies.AgentsPolicy;
import rinde.sim.core.simulation.policies.InteractionRules;
import rinde.sim.core.simulation.policies.ModelPolicy;
import rinde.sim.core.simulation.policies.TickListenerPolicy;
import rinde.sim.core.simulation.policies.TickListenerSerialPolicy;
import rinde.sim.core.simulation.policies.agents.MultiThreaded;
import rinde.sim.core.simulation.policies.agents.SingleThreaded;
import rinde.sim.core.simulation.policies.agents.areas.Areas;
import rinde.sim.core.simulation.policies.agents.areas2.Areas2;
import rinde.sim.core.simulation.time.TimeIntervalImpl;
import rinde.sim.core.simulation.time.TimeLapseHandle;
import rinde.sim.event.Event;
import rinde.sim.event.EventAPI;
import rinde.sim.event.EventDispatcher;

/**
 * Simulator is the core class of a simulation. It is responsible for managing
 * time which it does by periodically providing all his registered policies
 * with {@link TimeInterval} objects.
 * 
 * Further it provides methods to start and stop simulations.
 * The simulator also acts as a facade through which
 * {@link Model}s and objects can be added to the simulator, more info about
 * models can be found in {@link ModelManager}.
 * 
 * The configuration phase of the simulator looks as follows:
 * <ol>
 * <li>register models using {@link #register(Object)}</li>
 * <li>call {@link #configure()}
 * <li>register objects using {@link #register(Object)}</li>
 * <li>start simulation by calling {@link #start()}</li>
 * </ol>
 * Note that objects can not be registered <b>before</b> calling
 * {@link #configure()} and {@link Model}s can not be registered <b>after</b>
 * configuring.
 * 
 * 
 * @author Rinde van Lon (rinde.vanlon@cs.kuleuven.be)
 * @author dmerckx
 */
public class Simulator{
    private TimeIntervalImpl masterTime;
    
    protected final ModelPolicy modelPolicy;
    protected final AgentsPolicy timeUserPolicy;
    protected final TickListenerPolicy externalPolicy;
    
    private final RandomGenerator rnd;
    
    private TickPolicy activePolicy;
    
    
    /**
     * The logger of the simulator.
     */
    protected static final Logger LOGGER = LoggerFactory
            .getLogger(Simulator.class);

    /**
     * Enum that describes the possible types of events that the simulator can
     * dispatch.
     */
    public enum SimulatorEventType {
        /**
         * Indicates that the simulator has stopped.
         */
        STOPPED,

        /**
         * Indicates that the simulator has started.
         */
        STARTED,

        /**
         * Indicates that the simulator has been configured.
         */
        CONFIGURED
    }

    /**
     * Reference to the {@link EventAPI} of the Simulator. Can be used to add
     * listeners to events dispatched by the simulator. Simulator events are
     * defined in {@link SimulatorEventType}.
     */
    protected final EventAPI eventAPI;
    
    
    /**
     * Reference to dispatcher of simulator events, can be used by subclasses to
     * issue additional events.
     */
    protected final EventDispatcher dispatcher;

    /**
     * @see #isPlaying()
     */
    protected volatile boolean isPlaying;

    /**
     * Model manager instance.
     */
    protected final ModelManager modelManager;
    private boolean configured;

    private final long timeStep;
    
    public Simulator(long step){
        this(step, 19, null);
    }
    
    public Simulator(long step, long seed){
        this(step, seed, null);
    }
    
    public Simulator(long step, AgentsPolicy policy){
        this(step, 19, policy);
    }
    
    public Simulator(long step, long seed, AgentsPolicy policy) {
        masterTime = new TimeIntervalImpl(0, step);
        
        modelPolicy = new ModelPolicy();
        timeUserPolicy = policy == null? new SingleThreaded() : policy;
        externalPolicy = new TickListenerSerialPolicy(true);
        
        timeStep = step;
        modelManager = new ModelManager();

        dispatcher = new EventDispatcher(SimulatorEventType.values());
        eventAPI = dispatcher.getEventAPI();
        
        this.rnd = new MersenneTwister(seed);
    }
    
    /**
     * This configures the {@link Model}s in the simulator. After calling this
     * method models can no longer be added, objects can only be registered
     * after this method is called.
     * @see ModelManager#configure()
     */
    public void configure() {
        modelManager.configure();
        configured = true;
        dispatcher
                .dispatchEvent(new Event(SimulatorEventType.CONFIGURED, this));
        
        InteractionRules rules = timeUserPolicy.getInteractionRules();
        
        for(Model<?,?> model:modelManager.getModels()){
            model.init(rnd.nextLong(), rules, masterTime);
        }
        
        //TODO: skip this step when the roadmodel is not used
        timeUserPolicy.init(modelManager.getModel(RoadModel.class).getViewRect());
    }
    
    public void configureWithWarmup(){
        configure();
        timeUserPolicy.warmUp();
    }
    
    // ----- REGISTERING ----- // 
    
    public void registerModel(Model<?,?> model){
        assert model != null: "model cannot be null";
        assert !configured: "cannot register model after calling configure";
        
        modelManager.add(model);
        modelPolicy.register(model);
        
        LOGGER.debug("Model is added: " + model);
    }
    
    public void registerTickListener(TickListener listener){
        assert listener != null: "listener cannot be null";
        assert configured: "cannot register tick listener before calling configure";

        externalPolicy.register(listener);
    }
    
    public void unregisterTickListener(TickListener listener){
        assert listener != null: "listener cannot be null";
        assert configured: "cannot unregister tick listener before calling configure";
    
        externalPolicy.unregister(listener);
    }
    
    public void registerUser(User<Data> user) {
        registerUser(user, new Data() {});
    }
    
    public <D extends Data> void registerUser(User<D> user, D data) {
        assert user!=null: "object can not be null";
        assert data!=null: "data can not be null";
        assert configured: "cannot register users before calling configure";
        assert activePolicy == null || activePolicy.canRegisterDuringExecution():
                "Within " + activePolicy + " it is not possible to register objects";
        assert(!(user instanceof Model) && !(user instanceof TickListener)):
                "A user can not be a model or ticklistener";
       
        TimeLapseHandle handle = new TimeLapseHandle(masterTime);
        addUser(UserInit.create(user, data), handle);
        
        if(user instanceof Agent){
            timeUserPolicy.register((Agent) user, handle);
        }
    }
    
    Multimap<User<?>, User<?>> users = ArrayListMultimap.create();
    
    private <D extends Data> void addUser(UserInit<D> init, TimeLapseHandle lapse){
        List<UserInit<?>> guards = modelManager.register(init.user, init.data, lapse);
        
        for(UserInit<?> g:guards){
            users.put(init.user, g.user);
        }
        
        if(init.user instanceof InitUser){
            timeUserPolicy.addInituser((InitUser) init.user);
        }
        
        for(UserInit<?> g:guards){
            addUser(g, lapse);
        }
    }
    
    public void unregisterUser(User user){
        assert configured: "cannot unregister users before calling configure";
    
        removeUser(user);
        if(user instanceof Agent)
            timeUserPolicy.unregister((Agent) user);
    }
    
    private <D extends Data> void removeUser(User<?> user){
        modelManager.unregister(user);
        
        for(User<?> g:users.get(user)){
            removeUser(g);
        }
        
        users.removeAll(user);
    }

    /**
     * Returns a safe to modify list of all models registered in the simulator.
     * @return list of models
     */
    public List<Model<?,?>> getModels() {
        return modelManager.getModels();
    }

    /**
     * Returns the {@link ModelProvider} that has all registered models.
     * @return The model provider
     */
    public ModelProvider getModelProvider() {
        return modelManager;
    }

    public long getTimeStep(){
        return timeStep;
    }
    
    /**
     * @return The current simulation time.
     */
    public long getCurrentTime() {
        return masterTime.getEndTime();
    }
    
    /**
     * Start the simulation.
     */
    public void start() {
        assert configured: "Simulator can not be started when it is not configured";
    
        if (!isPlaying) {
            dispatcher.dispatchEvent(new Event(SimulatorEventType.STARTED, this));
        }
        isPlaying = true;
        while (isPlaying) {
            tick();
        }
        dispatcher.dispatchEvent(new Event(SimulatorEventType.STOPPED, this));
    }
    
    public void advanceTicks(int nrTicks){
        for(int i = 0; i < nrTicks; i++){
            advanceTick();
        }
    }
    
    public void advanceTick(){
        if( !isPlaying()){
            tick();
        }
    }
    
    private <T> void performTicks(TickPolicy policy, TimeInterval interval){
        activePolicy = policy;
        policy.performTicks(interval);
        activePolicy = null;
    } 

    /**
     * Advances the simulator with one step (the size is determined by the time
     * step).
     */
    private void tick() {
        Monitor.get().startModels();
        performTicks(modelPolicy, masterTime);
        Monitor.get().startModels();
        
        masterTime.nextStep();
        
        Monitor.get().startAgents();
        performTicks(timeUserPolicy, masterTime);
        Monitor.get().endAgents();
        
        Monitor.get().startModels();
        performTicks(externalPolicy, masterTime);
        Monitor.get().endModels();
    }

    /**
     * Either starts or stops the simulation depending on the current state.
     */
    public void togglePlayPause() {
        isPlaying = !isPlaying;
        if (isPlaying) {
            start();
        }
    }

    /**
     * Resets the time to 0.
     */
    public void resetTime() {
        throw new UnsupportedOperationException("TODO");
    }

    /**
     * Stops the simulation.
     */
    public void stop() {
        isPlaying = false;
    }
    
    public void shutdown(){
        stop();
        timeUserPolicy.shutDown();
    }

    /**
     * @return true if simulator is playing, false otherwise.
     */
    public boolean isPlaying() {
        return isPlaying;
    }

    public boolean isConfigured() {
        return configured;
    }
    
    /**
     * Reference to the {@link EventAPI} of the Simulator. Can be used to add
     * listeners to events dispatched by the simulator. Simulator events are
     * defined in {@link SimulatorEventType}.
     * @return {@link EventAPI}
     */
    public EventAPI getEventAPI() {
        return eventAPI;
    }
}
