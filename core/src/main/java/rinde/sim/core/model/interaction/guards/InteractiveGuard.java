package rinde.sim.core.model.interaction.guards;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import rinde.sim.core.model.Guard;
import rinde.sim.core.model.interaction.ExtendedReceiver;
import rinde.sim.core.model.interaction.InteractionModel;
import rinde.sim.core.model.interaction.Notification;
import rinde.sim.core.model.interaction.Result;
import rinde.sim.core.model.interaction.Visitor;
import rinde.sim.core.model.interaction.users.InteractiveAgent;
import rinde.sim.core.simulation.TimeInterval;
import rinde.sim.core.simulation.TimeLapse;

public class InteractiveGuard<N extends Notification> implements Guard {

    private List<N> notifications = new ArrayList<N>();
    private List<N> notificationsInbox = new ArrayList<N>();
    
    private final InteractiveAgent agent;
    private final InteractionModel interactionModel;
    
    private List<ExtendedReceiver<? extends N>> receivers = new ArrayList<ExtendedReceiver<? extends N>>();
    
    public InteractiveGuard(InteractiveAgent agent, InteractionModel model) {
        this.agent = agent;
        this.interactionModel = model;
    }
    
    public synchronized void receiveNotification(N notification){
        notifications.add(notification);
    }
    
    public synchronized void receiveTermination(ExtendedReceiver<N> receiver){
        receivers.remove(receiver);
    }
    
    public void afterTick(TimeInterval time) {
        if(notifications.size() == 0)
            return;
        
        notificationsInbox.addAll(notifications);
    }
    
    protected List<N> getNotifications(){
        return notificationsInbox;
    }
    
    public <R extends Result> R visit(TimeLapse lapse, Visitor<?, R> visitor){
        return interactionModel.visit(lapse, visitor);
    }
    
    public void advertise(ExtendedReceiver<? extends N> receiver){
        receivers.add(receiver);
    }
    
    public void removeReceiver(ExtendedReceiver<? extends N> receiver){
        interactionModel.remove(receiver);
        receivers.remove(receiver);
    }

    public void removeAll(Class<?> target) {
        Iterator<ExtendedReceiver<? extends N>> it = receivers.iterator();
        while(it.hasNext()){
            ExtendedReceiver<? extends N> receiver = it.next();
            if( receiver.getClass().isAssignableFrom(target)){
                interactionModel.remove(receiver);
                it.remove();
            }
        }
    }

    public void removeAll() {
        for(ExtendedReceiver<? extends N> receiver:receivers){
            interactionModel.remove(receiver);
        }
        receivers.clear();
    }
}
