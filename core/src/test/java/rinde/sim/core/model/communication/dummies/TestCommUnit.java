package rinde.sim.core.model.communication.dummies;

import javax.security.auth.callback.Callback;

import rinde.sim.core.graph.Point;
import rinde.sim.core.model.UnitImpl;
import rinde.sim.core.model.communication.Message;
import rinde.sim.core.model.communication.apis.CommunicationAPI;
import rinde.sim.core.model.communication.supported.CommUnit;
import rinde.sim.core.model.communication.users.CommUser;
import rinde.sim.core.model.road.apis.RoadAPI;

public class TestCommUnit extends UnitImpl implements CommUnit{

    public final TestCommUser user;
    public final Point pos;
    public final double rad;
    public final double rel;
    
    public RoadAPI roadAPI;
    public CommunicationAPI commAPI;
    
    public TestCommUnit(TestCommUser user, Point loc, double rad, double rel) {
        this.user = user;
        this.pos = loc;
        this.rad = rad;
        this.rel = rel;
    }

    @Override
    public void init() {
        user.roadAPI = roadAPI;
        user.commAPI = commAPI;
    }
    
    @Override
    public RoadAPI getRoadAPI() {
        return roadAPI;
    }

    @Override
    public void setRoadAPI(RoadAPI api) {
        this.roadAPI = api;
    }

    @Override
    public CommunicationAPI getCommunicationAPI() {
        return commAPI;
    }

    @Override
    public void setCommunicationAPI(CommunicationAPI api) {
        this.commAPI = api;
    }

    @Override
    public CommUser getElement() {
        return user;
    }

    @Override
    public CommData getInitData() {
        return new CommData() {
            
            @Override
            public Point getStartPosition() {
                return pos;
            }
            
            @Override
            public Double getInitialReliability() {
                return rel;
            }
            
            @Override
            public Double getInitialRadius() {
                return rad;
            }
        };
    }

}
