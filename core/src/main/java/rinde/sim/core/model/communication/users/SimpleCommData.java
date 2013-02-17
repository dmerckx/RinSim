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
    public static final SimpleCommData RELIABLE = new Std(1.0);
    
    /**
     * The reliability to be used to receiving and sending messages.
     * When X send a message to Y, the chance of the messages arriving
     * is the product of the reliability of X and the reliability of Y.
     * @return The reliability for sending and receiving messages.
     */
    Double getReliability();
    
    
    /**
     * Standard implementation of a {@link SimpleCommData}, can be used by
     * {@link SimpleCommUser}s that don't require extra initialization data.
     */
    public static class Std implements SimpleCommData{
        private final double reliability;
        
        /**
         * Create a new standard {@link SimpleCommData} with given reliability.
         * @param reliability The reliability to use.
         */
        @SuppressWarnings("hiding") 
        public Std(double reliability) {
            this.reliability = reliability;
        }
        
        @Override
        public Double getReliability() {
            return reliability;
        }
    }
}
