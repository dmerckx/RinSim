package rinde.sim.core.model.interaction;

import rinde.sim.core.graph.Point;
import rinde.sim.core.model.interaction.apis.InteractiveGuard;
import rinde.sim.core.model.pdp.PdpModel;

public abstract class Receiver{

    public final Point location;
    private InteractionModel model;
    
    public Receiver(Point location) {
        this.location = location;
    }
    
    public final void setModel(InteractionModel model){
        this.model = model;
    }
    
    public final void terminate(){
        model.remove(this);
    }
}
