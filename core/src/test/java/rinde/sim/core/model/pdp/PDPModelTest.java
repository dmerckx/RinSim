/**
 * 
 */
package rinde.sim.core.model.pdp;

import static com.google.common.collect.Lists.newLinkedList;
import static com.google.common.collect.Sets.newHashSet;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import rinde.sim.core.TimeLapseFactory;
import rinde.sim.core.graph.Point;
import rinde.sim.core.model.Model;
import rinde.sim.core.model.ModelProvider;
import rinde.sim.core.model.road.PlaneRoadModel;
import rinde.sim.core.model.road.RoadModel;
import rinde.sim.core.old.pdp.Depot_Old;
import rinde.sim.core.old.pdp.PDPModel;
import rinde.sim.core.old.pdp.PDPObject;
import rinde.sim.core.old.pdp.PDPType;
import rinde.sim.core.old.pdp.Parcel_Old;
import rinde.sim.core.old.pdp.Vehicle_Old;
import rinde.sim.core.old.pdp.PDPModel.PDPModelEventType;
import rinde.sim.core.old.pdp.PDPModel.ParcelState;
import rinde.sim.core.old.pdp.PDPModel.PickupAction;
import rinde.sim.core.old.pdp.PDPModel.VehicleState;
import rinde.sim.core.simulation.time.TimeLapse;
import rinde.sim.util.TimeWindow;

/**
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 * 
 */
@SuppressWarnings("javadoc")
public class PDPModelTest {

    static final double EPSILON = 0.0000001;

    PDPModel model;
    RoadModel rm;

    @Before
    public void setUp() {
        rm = new PlaneRoadModel(new Point(0, 0), new Point(10, 10), true,
                Double.POSITIVE_INFINITY);
        model = new PDPModel();
        model.registerModelProvider(new ModelProvider() {
            @SuppressWarnings("unchecked")
            @Override
            public <T extends Model<?>> T getModel(Class<T> clazz) {
                return (T) rm;
            }
        });

        /*
         * Added to remove noise in coverage tool.
         */
        PDPType.values();
        PDPType.valueOf("PARCEL");
        ParcelState.values();
        ParcelState.valueOf("AVAILABLE");
        VehicleState.values();
        VehicleState.valueOf("IDLE");
        PDPModelEventType.values();
        PDPModelEventType.valueOf("START_PICKUP");

        assertTrue(model.getSupportedType().equals(PDPObject.class));
    }

    @Test
    public void testPickup() {
        final Parcel_Old pack1 = new TestParcel(new Point(2, 2), 0, 0, 2);
        final Vehicle_Old truck = new TestVehicle(new Point(1, 1), 10.0, 1.0);
        model.register(pack1);
        model.register(truck);
        rm.register(pack1);
        rm.register(truck);
        rm.addObjectAt(pack1, new Point(1, 1));

        // nothing should happen
        truck.tick(TimeLapseFactory.create(0, 10000));

        assertEquals(0, model.getContentsSize(truck), EPSILON);
        assertTrue(model.getContents(truck).isEmpty());
        assertEquals(ParcelState.AVAILABLE, model.getParcelState(pack1));
        assertEquals(VehicleState.IDLE, model.getVehicleState(truck));
        model.pickup(truck, pack1, TimeLapseFactory.create(0, 10000));
        assertEquals(ParcelState.IN_CARGO, model.getParcelState(pack1));
        assertEquals(VehicleState.IDLE, model.getVehicleState(truck));

        assertFalse(rm.containsObject(pack1));
        assertTrue(model.containerContains(truck, pack1));
        assertEquals(2, model.getContentsSize(truck), EPSILON);
        assertTrue(model.getContents(truck).contains(pack1));
        assertEquals(1, model.getContents(truck).size());

        final Parcel_Old pack2 = new TestParcel(new Point(2, 2), 100, 100, 2);
        model.register(pack2);
        rm.register(pack2);
        rm.addObjectAt(pack2, new Point(1, 2));

        rm.followPath(truck, newLinkedList(asList(new Point(1, 2))), TimeLapseFactory
                .create(0, 3600000));

        assertEquals(new Point(1, 2), rm.getPosition(truck));
        assertEquals(new Point(1, 2), rm.getPosition(pack2));
        assertEquals(ParcelState.AVAILABLE, model.getParcelState(pack2));
        assertEquals(VehicleState.IDLE, model.getVehicleState(truck));
        model.pickup(truck, pack2, TimeLapseFactory.create(0, 40));
        assertFalse(rm.containsObject(pack2));
        final PickupAction action = (PickupAction) model.pendingVehicleActions
                .get(truck);
        assertFalse(action.isDone());
        assertEquals(60, action.timeNeeded());
        assertEquals(ParcelState.PICKING_UP, model.getParcelState(pack2));
        assertEquals(VehicleState.PICKING_UP, model.getVehicleState(truck));

        model.continuePreviousActions(truck, TimeLapseFactory.create(0, 40));
        assertFalse(action.isDone());
        assertEquals(20, action.timeNeeded());
        assertEquals(ParcelState.PICKING_UP, model.getParcelState(pack2));
        assertEquals(VehicleState.PICKING_UP, model.getVehicleState(truck));

        final TimeLapse tl = TimeLapseFactory.create(0, 40);
        model.continuePreviousActions(truck, tl);
        assertTrue(action.isDone());
        assertEquals(0, action.timeNeeded());
        assertEquals(20, tl.getTimeLeft());
        assertEquals(ParcelState.IN_CARGO, model.getParcelState(pack2));
        assertEquals(VehicleState.IDLE, model.getVehicleState(truck));

    }

    @Test
    public void testDelayedPickup() {

        final Parcel_Old pack1 = new TestParcel(new Point(2, 2), 10, 10, 2);
        final Vehicle_Old truck = new TestVehicle(new Point(1, 1), 10.0, 1.0);
        model.register(pack1);
        model.register(truck);
        rm.register(pack1);
        rm.register(truck);
        rm.addObjectAt(pack1, new Point(1, 1));

        model.pickup(truck, pack1, TimeLapseFactory.create(0, 1));
        assertTrue(model.getContents(truck).isEmpty());
        assertEquals(model.getContentsSize(truck), 0, EPSILON);

        truck.tick(TimeLapseFactory.create(1, 10));
        assertFalse(model.getContents(truck).isEmpty());
        assertTrue(model.getContents(truck).contains(pack1));
        assertEquals(model.getContentsSize(truck), 2, EPSILON);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPickupFail1() {
        // truck not in roadmodel
        final Vehicle_Old truck = new TestVehicle(new Point(1, 1), 10.0, 1.0);
        model.pickup(truck, null, TimeLapseFactory.create(0, 1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPickupFail2() {
        // package not in roadmodel
        final Vehicle_Old truck = new TestVehicle(new Point(1, 1), 10.0, 1.0);
        rm.register(truck);
        final Parcel_Old pack1 = new TestParcel(new Point(2, 2), 0, 0, 2.0);
        model.pickup(truck, pack1, TimeLapseFactory.create(0, 1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPickupFail3A() {
        // package not in available state (it is already been picked up)
        final Vehicle_Old truck = new TestVehicle(new Point(1, 1), 10.0, 1.0);
        rm.register(truck);
        model.register(truck);
        final Parcel_Old pack1 = new TestParcel(new Point(2, 2), 10, 10, 2.0);
        rm.register(pack1);
        model.register(pack1);
        rm.addObjectAtSamePosition(pack1, truck);
        assertTrue(rm.equalPosition(truck, pack1));
        assertEquals(ParcelState.AVAILABLE, model.getParcelState(pack1));
        assertEquals(VehicleState.IDLE, model.getVehicleState(truck));

        model.pickup(truck, pack1, TimeLapseFactory.create(0, 100));
        assertEquals(ParcelState.IN_CARGO, model.getParcelState(pack1));
        assertEquals(VehicleState.IDLE, model.getVehicleState(truck));

        // checks what happens when you add a package to the roadmodel which has
        // already been picked up
        rm.addObjectAt(pack1, new Point(1, 1));
        model.pickup(truck, pack1, TimeLapseFactory.create(0, 100));

    }

    @Test(expected = IllegalArgumentException.class)
    public void testPickupFail3B() {
        // package is not registered
        final Vehicle_Old truck = new TestVehicle(new Point(1, 1), 10.0, 1.0);
        rm.register(truck);
        final Parcel_Old pack1 = new TestParcel(new Point(2, 2), 0, 0, 2.0);
        rm.register(pack1);
        rm.addObjectAtSamePosition(pack1, truck);
        model.pickup(truck, pack1, TimeLapseFactory.create(0, 1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPickupFail4() {
        final Vehicle_Old truck = new TestVehicle(new Point(1, 1), 10.0, 1.0);
        rm.register(truck);
        final Parcel_Old pack1 = new TestParcel(new Point(2, 2), 0, 0, 2.0);
        rm.register(pack1);
        rm.addObjectAtSamePosition(pack1, truck);
        model.register(pack1);
        model.pickup(truck, pack1, TimeLapseFactory.create(0, 1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPickupFail5() {
        // wrong position
        final Vehicle_Old truck = new TestVehicle(new Point(1, 1), 10.0, 1.0);
        rm.register(truck);
        model.register(truck);
        final Parcel_Old pack1 = new TestParcel(new Point(2, 2), 0, 0, 2.0);
        rm.register(pack1);
        rm.addObjectAt(pack1, new Point(0, 0));
        model.register(pack1);
        model.pickup(truck, pack1, TimeLapseFactory.create(0, 1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPickupFail6A() {
        // package does not fit in truck
        final Vehicle_Old truck = new TestVehicle(new Point(1, 1), 1.0, 1.0);
        rm.register(truck);
        model.register(truck);
        final Parcel_Old pack1 = new TestParcel(new Point(2, 2), 0, 0, 2.0);
        rm.register(pack1);
        model.register(pack1);
        rm.addObjectAtSamePosition(pack1, truck);
        assertTrue(rm.equalPosition(truck, pack1));
        model.pickup(truck, pack1, TimeLapseFactory.create(0, 100));
    }

    @Test
    public void testPickupFail6B() {
        // last package does not fit in truck
        final Vehicle_Old truck = new TestVehicle(new Point(1, 1), 10.0, 1.0);
        rm.register(truck);
        model.register(truck);

        for (int i = 0; i < 9; i++) {
            final Parcel_Old newPack = new TestParcel(new Point(2, 2), 0, 0, 1.0);
            rm.register(newPack);
            model.register(newPack);
            rm.addObjectAtSamePosition(newPack, truck);
            model.pickup(truck, newPack, TimeLapseFactory.create(0, 1));
        }
        assertEquals(model.getContents(truck).size(), 9);
        assertEquals(model.getContentsSize(truck), 9.0, EPSILON);

        final Parcel_Old packTooMuch = new TestParcel(new Point(2, 2), 0, 0, 1.0);
        rm.register(packTooMuch);
        model.register(packTooMuch);
        rm.addObjectAtSamePosition(packTooMuch, truck);
        try {
            model.pickup(truck, packTooMuch, TimeLapseFactory.create(0, 1));
        } catch (final IllegalArgumentException e) {
            assertTrue(true);
        }

    }

    @Test
    public void testDeliver() {
        final Vehicle_Old truck = new TestVehicle(new Point(1, 1), 10.0, 1.0);
        rm.register(truck);
        model.register(truck);
        final Parcel_Old pack1 = new TestParcel(new Point(2, 2), 10, 10, 2.0);
        rm.register(pack1);
        model.register(pack1);
        rm.addObjectAtSamePosition(pack1, truck);
        assertTrue(rm.equalPosition(truck, pack1));
        assertEquals(ParcelState.AVAILABLE, model.getParcelState(pack1));
        assertEquals(VehicleState.IDLE, model.getVehicleState(truck));
        assertEquals(newHashSet(pack1), model.getAvailableParcels());

        model.pickup(truck, pack1, TimeLapseFactory.create(0, 100));
        assertEquals(ParcelState.IN_CARGO, model.getParcelState(pack1));
        assertEquals(VehicleState.IDLE, model.getVehicleState(truck));
        assertTrue(model.getAvailableParcels().isEmpty());

        rm.moveTo(truck, pack1.getDestination(), TimeLapseFactory
                .create(0, 3600000 * 3));
        assertEquals(pack1.getDestination(), rm.getPosition(truck));

        model.deliver(truck, pack1, TimeLapseFactory.create(0, 8));
        assertEquals(ParcelState.DELIVERING, model.getParcelState(pack1));
        assertEquals(VehicleState.DELIVERING, model.getVehicleState(truck));

        final TimeLapse tl = TimeLapseFactory.create(0, 10);
        model.continuePreviousActions(truck, tl);
        assertEquals(ParcelState.DELIVERED, model.getParcelState(pack1));
        assertEquals(VehicleState.IDLE, model.getVehicleState(truck));
        assertEquals(8, tl.getTimeLeft());

        final Parcel_Old pack2 = new TestParcel(new Point(2, 2), 10, 10, 2.0);
        rm.register(pack2);
        model.register(pack2);
        rm.addObjectAtSamePosition(pack2, truck);
        assertEquals(ParcelState.AVAILABLE, model.getParcelState(pack2));
        assertEquals(VehicleState.IDLE, model.getVehicleState(truck));
        assertEquals(newHashSet(pack2), model.getAvailableParcels());

        model.pickup(truck, pack2, TimeLapseFactory.create(0, 10));
        assertEquals(ParcelState.IN_CARGO, model.getParcelState(pack2));
        assertEquals(VehicleState.IDLE, model.getVehicleState(truck));
        assertTrue(model.getAvailableParcels().isEmpty());

        model.deliver(truck, pack2, TimeLapseFactory.create(0, 10));
        assertEquals(ParcelState.DELIVERED, model.getParcelState(pack2));
        assertEquals(VehicleState.IDLE, model.getVehicleState(truck));

    }

    @Test(expected = IllegalArgumentException.class)
    public void testDeliverFail1() {
        // truck does not exist in roadmodel
        model.deliver(new TestVehicle(new Point(1, 1), 20.0, 1.0), null, TimeLapseFactory
                .create(0, 1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDeliverFail2() {
        final Vehicle_Old truck = new TestVehicle(new Point(1, 1), 10.0, 1.0);
        rm.register(truck);
        model.register(truck);
        final Parcel_Old pack1 = new TestParcel(new Point(2, 2), 10, 10, 2.0);
        rm.register(pack1);
        model.register(pack1);
        rm.addObjectAtSamePosition(pack1, truck);

        model.pickup(truck, pack1, TimeLapseFactory.create(0, 1));
        assertEquals(VehicleState.PICKING_UP, model.getVehicleState(truck));
        model.deliver(truck, pack1, TimeLapseFactory.create(0, 1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDeliverFail3() {
        final Vehicle_Old truck = new TestVehicle(new Point(1, 1), 10.0, 1.0);
        rm.register(truck);
        model.register(truck);
        final Parcel_Old pack1 = new TestParcel(new Point(2, 2), 10, 10, 2.0);
        rm.register(pack1);
        model.register(pack1);
        rm.addObjectAtSamePosition(pack1, truck);

        model.deliver(truck, pack1, TimeLapseFactory.create(0, 1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDeliverFail4() {
        final Vehicle_Old truck = new TestVehicle(new Point(1, 1), 10.0, 1.0);
        rm.register(truck);
        model.register(truck);
        final Parcel_Old pack1 = new TestParcel(new Point(2, 2), 10, 10, 2.0);
        rm.register(pack1);
        model.register(pack1);
        rm.addObjectAtSamePosition(pack1, truck);

        model.pickup(truck, pack1, TimeLapseFactory.create(0, 10));
        assertTrue(model.getContents(truck).contains(pack1));
        model.deliver(truck, pack1, TimeLapseFactory.create(0, 1));
    }

    @Test
    public void addPackageIn() {
        assertTrue(model.getAvailableParcels().isEmpty());
        final Depot_Old d = new TestDepot(10);
        final Parcel_Old p1 = new TestParcel(new Point(0, 0), 0, 0, 1);
        model.register(d);
        model.register(p1);
        rm.addObjectAt(d, new Point(0, 0));
        model.addParcelIn(d, p1);

        assertTrue(model.getContents(d).size() == 1);
        assertTrue(model.getContents(d).contains(p1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void addPackageInFail1() {
        final Depot_Old d = new TestDepot(10);
        final Parcel_Old p1 = new TestParcel(new Point(0, 0), 0, 0, 1);
        rm.addObjectAt(p1, new Point(0, 0));
        model.addParcelIn(d, p1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void addPackageInFail2() {
        final Depot_Old d = new TestDepot(10);
        final Parcel_Old p1 = new TestParcel(new Point(0, 0), 0, 0, 1);
        model.addParcelIn(d, p1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void addPackageInFail3() {
        final Depot_Old d = new TestDepot(10);
        final Parcel_Old p1 = new TestParcel(new Point(0, 0), 0, 0, 1);
        model.register(p1);
        model.addParcelIn(d, p1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void addPackageInFail4() {
        final Depot_Old d = new TestDepot(10);
        final Parcel_Old p1 = new TestParcel(new Point(0, 0), 0, 0, 1);
        model.register(p1);
        model.register(d);
        model.addParcelIn(d, p1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void addPackageInFail5() {
        final Depot_Old d = new TestDepot(10);
        final Parcel_Old p1 = new TestParcel(new Point(0, 0), 0, 0, 11);
        model.register(p1);
        model.register(d);
        rm.addObjectAt(d, new Point(0, 0));
        model.addParcelIn(d, p1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void registerFail1() {
        final Parcel_Old p = new TestParcel(new Point(0, 0), 0, 0, 1.0);
        model.register(p);
        model.register(p);
    }

    @Test(expected = IllegalArgumentException.class)
    public void registerFail2() {
        final Depot_Old d = new TestDepot(10);
        model.register(d);
        model.register(d);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void unregisterFail() {
        model.unregister(null);
    }

    @Test(expected = IllegalStateException.class)
    public void containerSetCapacityFail() {
        final Depot_Old d = new TestDepot(10);
        model.register(d);
        rm.register(d);
        d.setCapacity(20);
    }

    @Test(expected = IllegalStateException.class)
    public void objectSetStartPositionFail() {
        final Depot_Old d = new TestDepot(10);
        model.register(d);
        assertEquals(model, d.getPDPModel());
        rm.register(d);
        assertEquals(rm, d.getRoadModel());
        d.setStartPosition(new Point(0, 0));
    }

    @Test(expected = IllegalStateException.class)
    public void objectCheckDoubleRegistrationFail1() {
        final Depot_Old d = new TestDepot(10);
        d.initPDPObject(model);
        d.initPDPObject(model);
    }

    @Test(expected = IllegalStateException.class)
    public void objectCheckDoubleRegistrationFail2() {
        final Depot_Old d = new TestDepot(10);
        d.initRoadUser(rm);
        d.initRoadUser(rm);
    }

    class TestParcel extends Parcel_Old {

        public TestParcel(Point pDestination, int pLoadingDuration,
                int pUnloadingDuration, double pMagnitude) {
            super(pDestination, pLoadingDuration, TimeWindow.ALWAYS,
                    pUnloadingDuration, TimeWindow.ALWAYS, pMagnitude);
        }

        /**
         * @param pLoadingDuration
         * @param pUnloadingDuration
         */
        public TestParcel(Point pDestination, int pLoadingDuration,
                TimeWindow pickupTW, int pUnloadingDuration,
                TimeWindow deliverTW, double pMagnitude) {
            super(pDestination, pLoadingDuration, pickupTW, pUnloadingDuration,
                    deliverTW, pMagnitude);
        }

        @Override
        public void initRoadPDP(RoadModel pRoadModel, PDPModel pPdpModel) {}

    }

    class TestVehicle extends Vehicle_Old {

        private final double speed;

        /**
         * @param startPos
         */
        public TestVehicle(Point startPos, double pCapacity, double pSpeed) {
            setStartPosition(startPos);
            setCapacity(pCapacity);
            speed = pSpeed;
        }

        @Override
        public double getSpeed() {
            return speed;
        }

        @Override
        protected void tickImpl(TimeLapse time) {}

        @Override
        public void afterTick(TimeLapse timeLapse) {}

        @Override
        public void initRoadPDP(RoadModel pRoadModel, PDPModel pPdpModel) {}
    }

    class TestDepot extends Depot_Old {

        public TestDepot(int pCapacity) {
            setCapacity(pCapacity);
        }

        @Override
        public void initRoadPDP(RoadModel pRoadModel, PDPModel pPdpModel) {}

    }
}
