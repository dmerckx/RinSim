package rinde.sim.core.model.interaction.supported;

import rinde.sim.core.model.interaction.Receiver;

public final class InteractiveReceiver implements InteractiveType{

    public final Receiver receiver;
    
    public InteractiveReceiver(Receiver receiver) {
        this.receiver = receiver;
    }
}
