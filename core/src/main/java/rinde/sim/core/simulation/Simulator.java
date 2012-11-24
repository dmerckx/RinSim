/**
 * 
 */
package rinde.sim.core.simulation;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rinde.sim.core.model.Agent;
import rinde.sim.core.model.Model;
import rinde.sim.core.model.ModelManager;
import rinde.sim.core.model.ModelProvider;
import rinde.sim.core.model.simulator.SimulatorModel;
import rinde.sim.core.simulation.policies.ParallelUnits;
import rinde.sim.core.simulation.policies.SerialInterval;
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

    private TickPolicy<?>[] policies;
    
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


    public static final TickPolicy<?>[] getStdPolicies(){
        TickPolicy<?>[] policies = new TickPolicy[3];
        
        policies[0] = new SerialInterval<PrimaryTickListener>(true, PrimaryTickListener.class);
        policies[1] = new ParallelUnits();
        policies[2] = new SerialInterval<ExternalTickListener>(false, ExternalTickListener.class);
        
        return policies;
    }
    
    public Simulator(long step) {
        this(step, getStdPolicies());
    }
    
    /**
     * @param step The stepsize used in between 2 ticks
     * @param policies The policies used to register/unregister/execute {@link TickListener}s
     */
    protected <F extends TickListener<?>> Simulator(long step, TickPolicy<?>[] policies) {
        timeStep = step;
        
        this.policies = policies;
        this.activePolicy = null;
        
        time = 0L;
        modelManager = new ModelManager();

        dispatcher = new EventDispatcher(SimulatorEventType.values());
        eventAPI = dispatcher.getEventAPI();
        
        register(new SimulatorModel());
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
    
    private void addTickListener(TickListener<?> listener) {
        for(TickPolicy<?> rule:policies){
            if(tryAddToPolicy(rule, listener))
                return;
        }
        throw new IllegalArgumentException("No policy rule found for: " + listener);
    }
    
    private <T extends TickListener<?>> boolean tryAddToPolicy(TickPolicy<T> rule, TickListener<?> listener){
        if( rule.getAcceptedType().isAssignableFrom(listener.getClass())){
            rule.register((T) listener);
            return true;
        }
        return false;
    }
    
    private void removeTickListener(TickListener<?> listener) {
        for(TickPolicy<?> rule:policies){
            if(tryRemoveFromPolicy(rule, listener))
                return;
        }
        throw new IllegalArgumentException("No policy rule found for: " + listener);
    }
    
    private <T extends TickListener<?>> boolean tryRemoveFromPolicy(TickPolicy<T> rule, TickListener<?> listener){
        if( rule.getAcceptedType().isAssignableFrom(listener.getClass())){
            rule.unregister((T) listener);
            return true;
        }
        return false;
    }
    
    /**
     * Register a model to the simulator.
     * @param model The {@link Model} instance to register.
     * @return true if succesful, false otherwise
     */
    private void registerModel(Model<?> model) {
        if (model == null) {
            throw new IllegalArgumentException("model can not be null");
        }
        if (configured) {
            throw new IllegalStateException(
                    "cannot add model after calling configure()");
        }
        final boolean result = modelManager.add(model);
        if (result) {
            LOGGER.info("registering model :" + model.getClass().getName()
                    + " for type:" + model.getSupportedType().getName());
        }
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
        if(activePolicy != null && !activePolicy.canRegisterDuringExecution())
            throw new IllegalStateException("Within the current policy (" + activePolicy + ")"
                    + "it is not possible to register objects");
        
        if (o == null) {
            throw new IllegalArgumentException("parameter can not be null");
        }
        
        if (o instanceof Model<?>) {    //Adding models
            registerModel((Model<?>) o);
        }
        else if (!configured) {         //Adding non-models before configure
            throw new IllegalStateException(
                    "can not add object before calling configure()");
        }
        else {                          //Adding non-models
            modelManager.register(o);
        }
        
        if (o instanceof TickListener) {
            addTickListener((TickListener) o);
        }
    }

    /**
     * Unregistration from the models is delayed until all ticks are processed.
     * Unregisters an object from the simulator.
     * @param o The object to be unregistered.
     */
    public void unregister(Object o) {
        if(activePolicy != null && !activePolicy.canUnregisterDuringExecution())
            throw new IllegalStateException("Within the current policy (" + activePolicy + ")"
                    + "it is not possible to unregister objects");
        
        if (o == null) {
            throw new IllegalArgumentException("parameter cannot be null");
        }
        if (o instanceof Model<?>) {
            throw new IllegalArgumentException("can not unregister a model");
        }
        if (!configured) {
            throw new IllegalStateException(
                    "can not unregister object before calling configure()");
        }
        if (o instanceof TickListener) {
            removeTickListener((Agent) o);
        }
        modelManager.unregister(o);
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
        if (!configured) {
            throw new IllegalStateException(
                    "Simulator can not be started when it is not configured.");
        }
        if (!isPlaying) {
            dispatcher
                    .dispatchEvent(new Event(SimulatorEventType.STARTED, this));
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

    /**
     * Advances the simulator with one step (the size is determined by the time
     * step).
     */
    private void tick() {
        long timeS = System.currentTimeMillis();
        
        TimeInterval interval = new TimeIntervalImpl(time, time+timeStep);
        
        for(TickPolicy<?> rule:policies){
            activePolicy = rule;
            rule.performTicks(interval);
        }
        activePolicy = null;
        
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

//    /**
//     * @return An unmodifiable view on the set of tick listeners.
//     */
//    public Collection<TickListener> getTickListeners(T rule) {
//        return Collections.unmodifiableCollection(listeners[rule.ordinal()]);
//    }
    
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
