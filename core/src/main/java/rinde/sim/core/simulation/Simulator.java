/**
 * 
 */
package rinde.sim.core.simulation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rinde.sim.core.model.Model;
import rinde.sim.core.model.ModelManager;
import rinde.sim.core.model.ModelProvider;
import rinde.sim.core.model.Unit;
import rinde.sim.core.model.User;
import rinde.sim.core.model.simulator.SimulatorModel;
import rinde.sim.core.simulation.policies.ModelPolicy;
import rinde.sim.core.simulation.policies.ParallelUnitsPolicy;
import rinde.sim.core.simulation.policies.TickListenerPolicy;
import rinde.sim.core.simulation.time.TimeIntervalImpl;
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

    protected final Map<User, Unit> unitMapping = new HashMap<User, Unit>();
    
    protected final TickPolicy<Model<?>> modelPolicy;
    protected final TickPolicy<Unit> unitsPolicy;
    protected final TickPolicy<TickListener> externalPolicy;
    
    private TickPolicy<?> activePolicy;
    
    
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
     * @see #getCurrentTime()
     */
    protected long time;

    /**
     * Model manager instance.
     */
    protected final ModelManager modelManager;
    private boolean configured;

    private final long timeStep;
    
    public Simulator(long step) {
        modelPolicy = new ModelPolicy();
        unitsPolicy = new ParallelUnitsPolicy();
        externalPolicy = new TickListenerPolicy(false);
        
        timeStep = step;
        time = 0L;
        modelManager = new ModelManager();

        dispatcher = new EventDispatcher(SimulatorEventType.values());
        eventAPI = dispatcher.getEventAPI();
        
        register(new SimulatorModel(this));
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
    }
    
    private void registerModel(Model<?> model){
        assert !configured: "cannot register model after calling configure";
        
        modelManager.add(model);
        modelPolicy.register(model);
        
        LOGGER.debug("Model is added: " + model);
    }
    
    private void registerUser(User user){
        assert configured: "cannot register users before calling configure";
        
        Unit unit = user.buildUnit();
        unitMapping.put(user, unit);
        modelManager.register(unit);
        unitsPolicy.register(unit);
    }
    
    private void unregisterUser(User user){
        assert configured: "cannot unregister users before calling configure";
    
        Unit unit = unitMapping.get(user);
        modelManager.unregister(unit);
        unitsPolicy.unregister(unit);
    }
    
    private void registerTickListener(TickListener listener){
        assert configured: "cannot register tick listener before calling configure";

        externalPolicy.register(listener);
    }
    
    private void unregisterTickListener(TickListener listener){
        assert configured: "cannot unregister tick listener before calling configure";
    
        externalPolicy.unregister(listener);
    }
    
    /**
     * Register a given entity in the simulator.
     * During registration the object is provided all features it requires
     * (declared by interfaces) and bound to the required models
     * (if they were registered in the simulator before).
     * @param o object to register
     * @throws IllegalStateException when simulator is not configured (by
     *             calling {@link Simulator#configure()}
     * @return <code>true</code> if object was added to at least one model
     */
    public void register(Object o) {
        assert o!=null: "object can not be null";
        assert activePolicy == null || activePolicy.canRegisterDuringExecution():
                "Within " + activePolicy + " it is not possible to register objects";
        
        if (o instanceof Model<?>) { 
            registerModel((Model<?>) o);
        }
        else if(o instanceof User){
            registerUser((User) o);
        }
        else if(o instanceof TickListener){
            registerTickListener((TickListener) o);
        }
        else {
            throw new IllegalArgumentException(o + " is of an unknown type.");
        }
    }

    /**
     * Unregisters an object from the simulator.
     * @param o The object to be unregistered.
     */
    public void unregister(Object o) {
        assert o!=null: "object can not be null";
        assert activePolicy == null || activePolicy.canUnregisterDuringExecution():
                "Within " + activePolicy + " it is not possible to unregister objects";
        
        if (o instanceof Model<?>) { 
            throw new IllegalArgumentException("Cannot unregister model");
        }
        else if(o instanceof User){
            unregisterUser((User) o);
        }
        else if(o instanceof TickListener){
            unregisterTickListener((TickListener) o);
        }
        else {
            throw new IllegalArgumentException(o + " is of an unknown type.");
        }
    }

    /**
     * Returns a safe to modify list of all models registered in the simulator.
     * @return list of models
     */
    public List<Model<?>> getModels() {
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
        return time;
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
    
    public void advanceTick(){
        if( !isPlaying()){
            tick();
        }
    }
    
    private <T> void performTicks(TickPolicy<T> policy, TimeInterval interval){
        activePolicy = policy;
        policy.performTicks(interval);
        activePolicy = null;
    } 

    /**
     * Advances the simulator with one step (the size is determined by the time
     * step).
     */
    private void tick() {
        long timeS = System.currentTimeMillis();
        
        TimeInterval interval = new TimeIntervalImpl(time, time+timeStep);
        
        performTicks(modelPolicy, interval);
        performTicks(unitsPolicy, interval);
        performTicks(externalPolicy, interval);
        
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("tick(): " + (System.currentTimeMillis() - timeS));
            timeS = System.currentTimeMillis();
        }

        time += timeStep;
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
        time = 0L;
    }

    /**
     * Stops the simulation.
     */
    public void stop() {
        isPlaying = false;
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
