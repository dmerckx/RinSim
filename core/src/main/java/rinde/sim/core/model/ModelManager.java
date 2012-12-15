/**
 * 
 */
package rinde.sim.core.model;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import rinde.sim.core.simulation.SimulatorToModelAPI;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;

/**
 * Models manager keeps track of all models used in the simulator. It is
 * responsible for adding a simulation object to the appropriate models
 * 
 * @author Bartosz Michalik <bartosz.michalik@cs.kuleuven.be>
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 * @author dmerckx
 */
public class ModelManager implements ModelProvider {

    private final Multimap<Class<?>, Model<?,?>> registry;
    private final List<Model<?,?>> models;
    private boolean configured;
    private SimulatorToModelAPI sim;
    
    /**
     * Instantiate a new model manager.
     */
    public ModelManager() {
        registry = LinkedHashMultimap.create();
        models = new LinkedList<Model<?,?>>();
    }

    /**
     * Adds a model to the manager. Note: a model can be added only once.
     * @param model The model to be added.
     * @return true when the addition was sucessful, false otherwise.
     * @throws IllegalStateException when method called after calling configure
     */
    public boolean add(Model<?,?> model) {
        assert model!=null : "Model can not be null";
        
        checkState(!configured, "model can not be registered after configure()");
        
        final Class<?> supportedType = model.getSupportedType();
        checkArgument(supportedType != null, "model must implement getSupportedType() and return a non-null");
        models.add(model);
        final boolean result = registry.put(supportedType, model);
        if (!result) {
            models.remove(model);
        }
        return result;
    }

    /**
     * Method that allows for initialization of the manager (e.g., resolution of
     * the dependencies between models) Should be called after all models were
     * registered in the manager.
     */
    public void configure() {
        for (final Model<?,?> m : models) {
            if (m instanceof ModelReceiver) {
                ((ModelReceiver) m).registerModelProvider(this);
            }
        }
        configured = true;
    }

    /**
     * Add object to all models that support a given object.
     * @param object object to register
     * @param <T> the type of object to register
     * @return <code>true</code> if object was added to at least one model
     */
    @SuppressWarnings("unchecked")
    public <D extends Data> void register(User<D> user, D data) {
        assert user!=null : "Can not register null";
        assert !(user instanceof Model): "Can not register models";
        
        checkState(configured, "can not register an object if configure() has not been called");

        final Set<Class<?>> modelSupportedTypes = registry.keySet();
        for (final Class<?> modelSupportedType : modelSupportedTypes) {
            if (modelSupportedType.isAssignableFrom(user.getClass())) {
                final Collection<Model<?,?>> assignableModels = registry
                        .get(modelSupportedType);
                for (final Model<?,?> m : assignableModels) {
                    ((Model<D,User<D>>) m).register(sim, user, data);
                }
            }
        }
    }

    /**
     * Unregister an object from all models it was attached to.
     * @param user object to unregister
     * @param <T> the type of object to unregister
     * @return <code>true</code> when the unregistration succeeded in at least
     *         one model
     * @throws IllegalStateException if the method is called before simulator is
     *             configured
     */
    @SuppressWarnings("unchecked")
    public <U extends User<?>> void unregister(U user) {
        assert user!=null : "Can not unregister null";
        assert !(user instanceof Model): "Can not unregister models";
        
        checkState(configured, "can not unregister when not configured, call configure() first");

        final Set<Class<?>> modelSupportedTypes = registry.keySet();
        for (final Class<?> modelSupportedType : modelSupportedTypes) {
            // check if object is from a known type
            if (modelSupportedType.isAssignableFrom(user.getClass())) {
                final Collection<Model<?,?>> assignableModels = registry
                        .get(modelSupportedType);
                for (final Model<?,?> m : assignableModels) {
                    ((Model<?,U>) m).unregister(user);
                }
            }
        }
    }

    /**
     * @return An unmodifiable view on all registered models.
     */
    public List<Model<?,?>> getModels() {
        return Collections.unmodifiableList(models);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Model<?,?>> T getModel(Class<T> clazz) {
        for (final Model<?,?> model : models) {
            if (clazz.isInstance(model)) {
                return (T) model;
            }
        }
        throw new IllegalArgumentException("There is no model of type: "
                + clazz);
    }
}
