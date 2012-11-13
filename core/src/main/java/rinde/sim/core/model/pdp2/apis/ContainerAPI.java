package rinde.sim.core.model.pdp2.apis;

import rinde.sim.core.model.pdp.Parcel;
import rinde.sim.core.model.pdp2.users.Container2;
import rinde.sim.core.model.pdp2.users.Truck;

public interface ContainerAPI {

    /**
     * The possible states a {@link Container} can be in.
     */
    public enum ContainerState {
        /**
         * The 'normal' state, indicating that a {@link Container2} is neither in
         * {@link #PICKING_UP} nor in {@link #DELIVERING} state.
         */
        IDLE,
        /**
         * State that indicates that a {@link Container2} is currently picking up
         * a {@link Parcel} from another {@link Container2}.
         */
        PICKING_UP,
        /**
         * State that indicates that the {@link Container2} is currently delivering
         * a {@link Parcel} to another {@link Container2}.
         */
        DELIVERING
    }
}
