package rinde.sim.core.model.pdp.supported;

import rinde.sim.core.graph.Point;
import rinde.sim.core.model.UnitImpl;
import rinde.sim.core.model.interaction.apis.InteractiveAPI;
import rinde.sim.core.model.pdp.Parcel;
import rinde.sim.core.model.pdp.PdpAPI;
import rinde.sim.core.model.pdp.apis.ContainerAPI;
import rinde.sim.core.model.pdp.users.Depot;
import rinde.sim.core.model.road.apis.RoadAPI;
import rinde.sim.core.simulation.TimeInterval;

public class DepotUnit<P extends Parcel> extends UnitImpl
                        implements ContainerUnit<P> {

    private Depot<P> element;
    private RoadAPI roadAPI;
    private InteractiveAPI interactiveAPI;
    private ContainerAPI<P> containerAPI;
    private PdpAPI pdpAPI;
    
    //Initial data
    public final Point initPos;
    public final double initCap;
    public final Class<P> parcelType;
    
    public DepotUnit(Depot<P> element, Point pos, double cap, Class<P> type) {
        this.element = element;
        this.initPos = pos;
        this.initCap = cap;
        this.parcelType = type;
    }

    public void init() {
        element.setRoadAPI(roadAPI);
        element.setContainerAPI(containerAPI);
    }

    @Override
    public Depot<P> getElement() {
        return element;
    }

    @Override
    public InteractiveAPI getInteractiveAPI() {
        return interactiveAPI;
    }

    @Override
    public void setInteractiveAPI(InteractiveAPI api) {
        this.interactiveAPI = api;
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
    public ContainerAPI<P> getContainerAPI() {
        return containerAPI;
    }

    @Override
    public void setContainerAPI(ContainerAPI<P> api) {
        this.containerAPI = api;
    }

    @Override
    public PdpAPI getPdpAPI() {
        return pdpAPI;
    }

    @Override
    public void setPdpAPI(PdpAPI api) {
        this.pdpAPI = api;
    }

    @Override
    public ContainerData<P> getInitData() {
        return new ContainerData<P>() {
            @Override
            public Point getStartPosition() {
                return initPos;
            }

            @Override
            public double getCapacity() {
                return initCap;
            }

            @Override
            public Class<P> getParcelType() {
                return parcelType;
            }
        };
    }
}