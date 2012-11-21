package rinde.sim.core.model.interaction.apis;

import java.util.List;

import rinde.sim.core.model.interaction.Notification;
import rinde.sim.core.model.interaction.Receiver;
import rinde.sim.core.model.interaction.Result;
import rinde.sim.core.model.interaction.Visitor;
import rinde.sim.core.simulation.TimeLapse;

public interface InteractiveAPI {
    
    public List<Notification> getNotifications();
    
    public <R extends Result> R visit(TimeLapse lapse, Visitor<?, R> visitor);
    
    public void advertise(Receiver receiver);
    
    public void removeReceiver(Receiver receiver);
    
    public void removeAll(Class<?> target);

    public void removeAll();
}
