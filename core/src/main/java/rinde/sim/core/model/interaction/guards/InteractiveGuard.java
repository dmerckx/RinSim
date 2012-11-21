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
import rinde.sim.core.model.interaction.apis.InteractiveAPI;
import rinde.sim.core.model.interaction.users.InteractiveUser;
import rinde.sim.core.simulation.TimeInterval;
import rinde.sim.core.simulation.TimeLapse;

public class InteractiveGuard implements Guard, InteractiveAPI {

    private List<Notification> notifications = new ArrayList<Notification>();
    private List<Notification> notificationsInbox = new ArrayList<Notification>();
    
    private final InteractiveUser agent;
    private final InteractionModel interactionModel;
    
    private List<ExtendedReceiver> receivers = new ArrayList<ExtendedReceiver>();
    
    public InteractiveGuard(InteractiveUser agent, InteractionModel model) {
        this.agent = agent;
        this.interactionModel = model;
    }
    
    public synchronized void receiveNotification(Notification notification){
        notifications.add(notification);
    }
    
    public synchronized void receiveTermination(ExtendedReceiver receiver){
        receivers.remove(receiver);
    }
    
    public void afterTick(TimeInterval time) {
        if(notifications.size() == 0)
            return;
        
        notificationsInbox.addAll(notifications);
    }
    
    @Override
    public List<Notification> getNotifications(){
        return notificationsInbox;
    }
    
    @Override
    public <R extends Result> R visit(TimeLapse lapse, Visitor<?, R> visitor){
        return interactionModel.visit(lapse, visitor);
    }
    
    @Override
    public void advertise(ExtendedReceiver receiver){
        receivers.add(receiver);
    }
    
    @Override
    public void removeReceiver(ExtendedReceiver receiver){
        interactionModel.remove(receiver);
        receivers.remove(receiver);
    }

    @Override
    public void removeAll(Class<?> target) {
        Iterator<ExtendedReceiver> it = receivers.iterator();
        while(it.hasNext()){
            ExtendedReceiver receiver = it.next();
            if( receiver.getClass().isAssignableFrom(target)){
                interactionModel.remove(receiver);
                it.remove();
            }
        }
    }

    @Override
    public void removeAll() {
        for(ExtendedReceiver receiver:receivers){
            interactionModel.remove(receiver);
        }
        receivers.clear();
    }
}
