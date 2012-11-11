package rinde.sim.scenario;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Sets.newHashSet;
import static java.util.Arrays.asList;

import java.util.Queue;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rinde.sim.core.simulation.Simulator;
import rinde.sim.core.simulation.TimeInterval;
import rinde.sim.core.simulation.types.ExternalTickListener;
import rinde.sim.event.Event;
import rinde.sim.event.EventAPI;
import rinde.sim.event.EventDispatcher;
import rinde.sim.event.Listener;

/**
 * A scenario controller represents a single simulation run. This class is
 * intended for extension.
 * 
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 * @author Bartosz Michalik <bartosz.michalik@cs.kuleuven.be>
 * @since 2.0
 */
public abstract class ScenarioController implements ExternalTickListener {

    /**
     * Logger for this class.
     */
    protected static final Logger LOGGER = LoggerFactory
            .getLogger(ScenarioController.class);

    /**
     * The {@link Event} types which can be dispatched by this class.
     * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
     */
    public enum EventType {
        /**
         * Dispatched when the scenario starts playing.
         */
        SCENARIO_STARTED,
        /**
         * Dispatched when the scenario has finished playing.
         */
        SCENARIO_FINISHED;
    }

    /**
     * Provides access to the {@link Event} API, allows adding and removing
     * {@link Listener}s that are notified when {@link ScenarioController}
     * dispatches {@link Event}s.
     */
    public final EventAPI eventAPI;

    /**
     * The scenario that is played.
     */
    protected final Scenario scenario;
    protected final Queue<TimedEvent> scenarioQueue;

    Simulator simulator;

    private int ticks;
    protected final EventDispatcher disp;
    private EventType status;

    /**
     * <code>true</code> when user interface was defined.
     */
    private boolean uiMode;

    /**
     * Create an instance of ScenarioController with defined {@link Scenario}
     * and number of ticks till end. If the number of ticks is negative the
     * simulator will run until the {@link Simulator#stop()} method is called.
     * TODO refine documentation
     * 
     * @param scen to realize
     * @param numberOfTicks when negative the number of tick is infinite
     */
    public ScenarioController(final Scenario scen, int numberOfTicks) {
        checkArgument(scen != null, "scenario can not be null");
        ticks = numberOfTicks;
        scenario = scen;// new Scenario(scen);
        scenarioQueue = scenario.asQueue();

        final Set<Enum<?>> typeSet = newHashSet(scenario
                .getPossibleEventTypes());
        typeSet.addAll(asList(EventType.values()));
        disp = new EventDispatcher(typeSet);
        eventAPI = disp.getEventAPI();
        disp.addListener(new TimedEventHandler(), scenario
                .getPossibleEventTypes());
    }

    /**
     * Method that initializes the simulator using
     * {@link ScenarioController#createSimulator()} and user interface (if
     * defined) using {@link ScenarioController#createUserInterface()}. Must be
     * called from within a constructor of specialized class.
     * @throws ConfigurationException
     */
    final protected void initialize() throws ConfigurationException {
        try {
            simulator = createSimulator();
        } catch (final Exception e) {
            LOGGER.warn("exception thrown during createSimulator()", e);
            throw new ConfigurationException(
                    "An exception was thrown while instantiating the simulator",
                    e);
        }
        checkSimulator();
        simulator.configure();
        LOGGER.info("simulator created");

        simulator.register(this);

        uiMode = createUserInterface();
    }

    /**
     * Access the simulator from the subclasses. Method returns simulator only
     * after calling {@link ScenarioController#initialize()}.
     * @return simulator or <code>null</code>
     */
    public Simulator getSimulator() {
        return simulator;
    }

    /**
     * Create simulator that will run the scenario.
     * 
     * @postcondition simulator != null && simulator not configured
     * @return simulator
     * @throws Exception
     */
    protected abstract Simulator createSimulator() throws Exception;

    /**
     * Create the user interface. By default method is empty and disables uiMode
     * 
     * @precondition simulator != null and simulator is configured
     * @return uiMode. should be <code>true</code> when user interface was
     *         created.
     */
    protected boolean createUserInterface() {
        return false;
    }

    /**
     * Stop the simulation.
     */
    public void stop() {
        if (!uiMode) {
            simulator.unregister(this);
            simulator.stop();
        }
    }

    /**
     * Starts the simulation.
     * @throws ConfigurationException If the scenario controller was not
     *             configured properly.
     */
    public void start() throws ConfigurationException {
        checkSimulator();
        if (ticks != 0 && !uiMode) {
            // new Thread() {
            // @Override
            // public void run() {
            simulator.start();
            // }
            // }.start();
        }

    }

    /**
     * @return <code>true</code> if all events of this scenario have been
     *         dispatched, <code>false</code> otherwise.
     */
    public boolean isScenarioFinished() {
        return scenarioQueue.isEmpty();
    }

    @Override
    final public void tick(TimeInterval timeLapse) {
        if (!uiMode && ticks == 0) {
            LOGGER.info("scenario finished at virtual time:"
                    + timeLapse.getStartTime() + "[stopping simulation]");
            simulator.stop();
        }
        if (LOGGER.isDebugEnabled() && ticks >= 0) {
            LOGGER.debug("ticks to end: " + ticks);
        }
        if (ticks > 0) {
            ticks--;
        }
        TimedEvent e = null;

        while ((e = scenarioQueue.peek()) != null
                && e.time <= timeLapse.getStartTime()) {
            scenarioQueue.poll();
            if (status == null) {
                LOGGER.info("scenario started at virtual time:"
                        + timeLapse.getStartTime());
                status = EventType.SCENARIO_STARTED;
                disp.dispatchEvent(new Event(status, this));
            }
            e.setIssuer(this);
            disp.dispatchEvent(e);
        }
        if (e == null && status != EventType.SCENARIO_FINISHED) {
            if (ticks == 0) {
                LOGGER.info("scenario finished at virtual time:"
                        + timeLapse.getStartTime() + "[stopping simulation]");
                simulator.stop();
            } else {
                LOGGER.info("scenario finished at virtual time:"
                        + timeLapse.getStartTime()
                        + " [scenario controller is detaching from simulator..]");
            }
            status = EventType.SCENARIO_FINISHED;
            simulator.unregister(this);
            disp.dispatchEvent(new Event(status, this));
        }

    }

    class TimedEventHandler implements Listener {

        public TimedEventHandler() {}

        @Override
        public final void handleEvent(Event e) {
            if (!handleTimedEvent((TimedEvent) e)) {
                LOGGER.warn("event not handled: " + e.toString());
                throw new IllegalArgumentException("event not handled: "
                        + e.toString());
            }
        }
    }

    /**
     * Should be overridden to handle all types of {@link TimedEvent}s
     * dispatched by the {@link Scenario}.
     * @param event The {@link TimedEvent} that is received.
     * @return <code>true</code> when the event is handled, <code>false</code>
     *         otherwise.
     */
    protected abstract boolean handleTimedEvent(TimedEvent event);

    private void checkSimulator() throws ConfigurationException {
        if (simulator == null) {
            throw new ConfigurationException(
                    "use createSimulator() to define simulator and make sure initialize() is called before calling start()");
        }
    }
}
