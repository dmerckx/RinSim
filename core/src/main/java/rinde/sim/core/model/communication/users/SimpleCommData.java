package rinde.sim.core.model.communication.users;

import rinde.sim.core.model.Data;

/**
 * Initialization data for {@link SimpleCommUser}s.
 * 
 * @author dmerckx
 */
public interface SimpleCommData extends Data {
    
    /**
     * The simplest form of a {@link SimpleCommData}, initialized with
     * reliability at 100%.
     */
    public static final SimpleCommData RELIABLE = new SimpleCommData() {
        @Override
        public Double getReliability() {
            return 1.0d;
        }
    };
    
    /**
     * The reliability to be used to receiving and sending messages.
     * When X send a message to Y, the chance of the messages arriving
     * is the product of the reliability of X and the reliability of Y.
     * @return The reliability for sending and receiving messages.
     */
    Double getReliability();
}
