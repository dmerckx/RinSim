/**
 * 
 */
package rinde.sim.core.model.road;

import static com.google.common.collect.Lists.newLinkedList;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import rinde.sim.core.graph.Connection;
import rinde.sim.core.graph.ConnectionData;
import rinde.sim.core.graph.Graph;
import rinde.sim.core.graph.Graphs;
import rinde.sim.core.graph.LengthData;
import rinde.sim.core.graph.MultiAttributeData;
import rinde.sim.core.graph.MultimapGraph;
import rinde.sim.core.graph.Point;
import rinde.sim.core.graph.TestMultimapGraph;
import rinde.sim.core.graph.TestTableGraph;
import rinde.sim.core.model.road.GraphRoadModel.Loc;
import rinde.sim.core.model.road.users.MovingRoadUser;
import rinde.sim.core.model.road.users.RoadUser;
import rinde.sim.util.TimeUnit;

/**
 * @author Rinde van Lon (rinde.vanlon@cs.kuleuven.be)
 * 
 */
@RunWith(Parameterized.class)
public class GraphRoadModelTest extends AbstractRoadModelTest<GraphRoadModel> {

    protected Class<? extends Graph<?>> graphType;
    protected Graph<? extends ConnectionData> graph;
    protected Class<? extends GraphRoadModel> roadModelType;

    // TODO what about negative speeds? and what about negative speed limits?

    static boolean connectionEquals(Connection<? extends ConnectionData> conn,
            Point from, Point to) {
        return conn.from.equals(from) && conn.to.equals(to);
    }

    public GraphRoadModelTest(Class<? extends Graph<?>> pGraphType,
            Class<? extends GraphRoadModel> pRoadModelType) {
        graphType = pGraphType;
        roadModelType = pRoadModelType;
    }

    @Parameters
    public static Collection<Object[]> configs() {
        return Arrays.asList(new Object[][] {
                { TestMultimapGraph.class, GraphRoadModel.class },
                { TestMultimapGraph.class, CachedGraphRoadModel.class },
                { TestTableGraph.class, GraphRoadModel.class },
                { TestTableGraph.class, CachedGraphRoadModel.class } });
    }

    @Override
    @Before
    public void setUp() throws InstantiationException, IllegalAccessException,
            IllegalArgumentException, SecurityException,
            InvocationTargetException, NoSuchMethodException {
        graph = (graphType.newInstance());
        model = roadModelType.getConstructor(Graph.class).newInstance(graph);

        graph.addConnection(SW, SE);
        graph.addConnection(SE, NE);
        graph.addConnection(NE, NW);

        final Set<Point> points = graph.getNodes();
        assertEquals(4, points.size());
        assertTrue(points.contains(SW));
        assertTrue(points.contains(SE));
        assertTrue(points.contains(NE));

        assertEquals(3, graph.getNumberOfConnections());
        assertEquals(4, graph.getNumberOfNodes());
    }

    @Test(expected = InvocationTargetException.class)
    public void constructorFail() throws IllegalArgumentException,
            SecurityException, InstantiationException, IllegalAccessException,
            InvocationTargetException, NoSuchMethodException {
        roadModelType.getConstructor(Graph.class).newInstance((Graph<?>) null);
    }

    /**
     * Follow path
     */
    @Test
    public void followPathAllAtOnce() {
        final SpeedyRoadUser agent = new SpeedyRoadUser(sc.from(5, TimeUnit.MS)
                .to(TimeUnit.H), SW);
        model.register(agent.buildUnit());
        assertEquals(SW, model.getPosition(agent));

        final Queue<Point> path = asPath(SW, SE, NE);
        final MoveProgress travelled = model
                .followPath(agent, path, timeLength(5));
        assertEquals(20, travelled.distance, EPSILON);
        assertEquals(4, travelled.time);
        assertEquals(0, path.size());
        assertEquals(NE, model.getPosition(agent));
    }

    @Test
    public void followLongPath() {
        final Point A = new Point(10, 20);
        final Point B = new Point(10, 22);
        final Point C = new Point(0, 15);
        Graphs.addPath(graph, NE, A, B, C);

        final TestRoadUser ab = new TestRoadUser(A);
        model.register(ab.buildUnit());
        model.followPath(ab, asPath(B), hour());
        assertEquals(A, model.getConnection(ab).from);
        assertEquals(B, model.getConnection(ab).to);

        final TestRoadUser bc = new TestRoadUser(B);
        model.register(bc.buildUnit());
        model.followPath(bc, asPath(C), hour());
        assertEquals(B, model.getConnection(bc).from);
        assertEquals(C, model.getConnection(bc).to);

        final Queue<Point> path = asPath(SW, SE, NE, A, model.getPosition(ab), B, model
                .getPosition(bc), C);
        assertEquals(8, path.size());

        final TestRoadUser driver1 = new TestRoadUser(SW);
        model.register(driver1.buildUnit());
        final MoveProgress pp = model.followPath(driver1, path, hour(44));
        assertEquals(hour(44).getTimeStep(), pp.time);
        assertEquals(44, pp.distance, EPSILON);
        assertEquals(asList(SW, SE, NE, A, B), pp.travelledNodes);
    }

    @Test(expected = IllegalArgumentException.class)
    public void followPathToMiddleAndBackFail() {
        final TestRoadUser middle = new TestRoadUser(SW);
        model.register(middle.buildUnit());
        model.followPath(middle, asPath(SE), hour());
        assertTrue(connectionEquals(model.getConnection(middle), SW, SE));

        final TestRoadUser driver = new TestRoadUser(SW);
        model.register(driver.buildUnit());
        model.followPath(driver, asPath(model.getPosition(middle), SW), hour(10));
    }

    @Test(expected = IllegalArgumentException.class)
    public void followPathUnconnected() {
        final MovingRoadUser agent = new SpeedyRoadUser(100);
        model.register(agent.buildUnit());
        assertEquals(new Point(0, 0), model.getPosition(agent));

        // illegal trajectory, the two points are not connected
        final Queue<Point> traject = new LinkedList<Point>(
                Arrays.asList(new Point(0, 0), new Point(10, 10)));

        assertEquals(2, traject.size());
        model.followPath(agent, traject, timeLength(20));
    }

    @Test(expected = IllegalArgumentException.class)
    public void followPathNotAvertex() {
        final MovingRoadUser agent = new SpeedyRoadUser(EPSILON);
        model.register(agent.buildUnit());
        assertEquals(new Point(0, 0), model.getPosition(agent));

        // illegal trajectory, the second point is not a vertex
        final Queue<Point> traject = new LinkedList<Point>(
                Arrays.asList(new Point(0, 0), new Point(10, 1)));

        assertEquals(2, traject.size());
        model.followPath(agent, traject, timeLength(20));
    }

    @Test
    public void followPathNotTillEnd() {
        final MovingRoadUser agent = new SpeedyRoadUser(sc.from(1, TimeUnit.MS)
                .to(TimeUnit.H));
        model.register(agent.buildUnit());
        assertEquals(new Point(0, 0), model.getPosition(agent));

        final Queue<Point> path = asPath(SW, SE, NE);
        MoveProgress travelled = model.followPath(agent, path, timeLength(10));
        assertEquals(10d, travelled.distance, EPSILON);
        assertEquals(1, path.size());

        travelled = model.followPath(agent, path, timeLength(1));
        assertEquals(1d, travelled.distance, EPSILON);
        assertEquals(1, path.size());
        assertEquals(new Point(10, 1), model.getPosition(agent));
    }

    /**
     * Simplest check for time based following path
     * {@link RoadModel#followPath(MovingRoadUser, Queue, long)}
     */
    @Test
    public void followPathTime() {
        final Queue<Point> path = asPath(SW, SE, NE);
        assertEquals(3, path.size());

        final MovingRoadUser agent = new SpeedyRoadUser(sc.from(5, TimeUnit.MS)
                .to(TimeUnit.H));
        model.register(agent.buildUnit());
        assertTrue(model.getPosition(agent).equals(new Point(0, 0)));
        assertEquals(3, path.size());

        MoveProgress progress = model.followPath(agent, path, timeLength(1));
        assertEquals(5d, progress.distance, EPSILON);
        assertEquals(2, path.size());
        assertEquals(new Point(5, 0), model.getPosition(agent));

        progress = model.followPath(agent, path, timeLength(2)); // follow path
                                                                 // for 2 x
                                                                 // time
        assertEquals(10, progress.distance, EPSILON);
        assertEquals(1, path.size());
        assertEquals(new Point(10, 5), model.getPosition(agent));

        progress = model.followPath(agent, path, timeLength(3)); // follow path
                                                                 // for 3 x
                                                                 // time
        // == 15
        assertEquals(5, progress.distance, EPSILON);
        assertEquals(1, progress.time);
        assertEquals(0, path.size());
        assertEquals(new Point(10, 10), model.getPosition(agent));
    }

    @Test
    public void followPathHalfway1() {
        graph.addConnection(SE, SW);
        graph.addConnection(NE, SE);

        final MovingRoadUser agent1 = new TestRoadUser(SW);
        model.register(agent1.buildUnit());
        model.followPath(agent1, new LinkedList<Point>(asList(SW, SE)), timeLength(TimeUnit.H
                .toMs(5)));
        assertEquals(new Point(5, 0), model.getPosition(agent1));

        final MovingRoadUser agent2 = new TestRoadUser(SW);
        model.register(agent2.buildUnit());
        assertEquals(new Point(0, 0), model.getPosition(agent2));

        final Queue<Point> path1 = new LinkedList<Point>(
                model.getShortestPathTo(agent2, agent1));
        assertEquals(asList(SW, new Point(5, 0)), path1);

        model.followPath(agent2, path1, timeLength(TimeUnit.H.toMs(10)));
        assertEquals(new Point(5, 0), model.getPosition(agent2));

        final Queue<Point> path2 = new LinkedList<Point>(
                model.getShortestPathTo(agent2, NE));
        assertEquals(asList(new Point(5, 0), SE, NE), path2);
        model.followPath(agent2, path2, timeLength(TimeUnit.H.toMs(10)));
        assertEquals(new Point(10, 5), model.getPosition(agent2));
        assertTrue(connectionEquals(model.getConnection(agent2), SE, NE));

        // coming from the front side, we have to turn around at p1
        final Queue<Point> path3 = new LinkedList<Point>(
                model.getShortestPathTo(agent2, agent1));
        assertEquals(asList(new Point(10, 5), NE, SE, SW, new Point(5, 0)), path3);
        model.followPath(agent2, path3, timeLength(TimeUnit.H.toMs(100)));

        assertEquals(new Point(5, 0), model.getPosition(agent1));
        assertEquals(new Point(5, 0), model.getPosition(agent2));

        graph.addConnection(SW, NW);
        graph.addConnection(NW, SW);
        model.followPath(agent2, new LinkedList<Point>(asList(SE, SW, NW)), timeLength(TimeUnit.H
                .toMs(25)));
        assertEquals(new Point(0, 10), model.getPosition(agent2));

        // coming from the back side, no turning around is required
        final Queue<Point> path4 = new LinkedList<Point>(
                model.getShortestPathTo(agent2, agent1));
        assertEquals(asList(NW, SW, new Point(5, 0)), path4);
        assertEquals(10, model.followPath(agent2, path4, timeLength(TimeUnit.H
                .toMs(10))).distance, EPSILON);
        assertEquals(new Point(0, 0), model.getPosition(agent2));
        assertEquals(5, model.followPath(agent2, path4, timeLength(TimeUnit.H
                .toMs(20))).distance, EPSILON);
        assertEquals(new Point(5, 0), model.getPosition(agent2));
    }

    @Test(expected = IllegalArgumentException.class)
    public void followPathFailObject1() {
        model.followPath(null, null, emptyTimeLapse);
    }

    @Test(expected = IllegalArgumentException.class)
    public void followPathFailObject2() {
        model.followPath(new TestRoadUser(), null, emptyTimeLapse);
    }

    @Test(expected = IllegalArgumentException.class)
    public void followPathWrongFirstPoint() {
        final MovingRoadUser agent = new SpeedyRoadUser(3, new Point(10,10));
        model.register(agent.buildUnit());

        final Queue<Point> path = asPath(SW, SE, NE);
        assertEquals(new Point(10, 10), model.getPosition(agent));
        assertEquals(3, path.size());
        // the path does not directly connect to the current position
        model.followPath(agent, path, timeLength(1));
    }

    /**
     * Situation: <code>SW ->- tru1 ->- SE </code><br/>
     * tru1 wants to move directly to SW, which should throw an exception since
     * moving backward over an directed edge is not allowed.
     */
    @Test(expected = IllegalArgumentException.class)
    public void followPathFailIllegalPath1() {
        final TestRoadUser testRoadUser = new TestRoadUser(SW);
        model.register(testRoadUser.buildUnit());
        final Queue<Point> p = new LinkedList<Point>(Arrays.asList(SW, SE));
        final MoveProgress progress = model
                .followPath(testRoadUser, p, timeLength(3600000));
        assertEquals(1.0, progress.distance, EPSILON);
        assertEquals(asList(SE), p);
        assertEquals(3600000, progress.time);
        assertEquals(asList(SW), progress.travelledNodes);
        model.followPath(testRoadUser, asPath(SW), timeLength(1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void followPathFailIllegalPath2() {
        final TestRoadUser testRoadUser1 = new TestRoadUser(SW);
        model.register(testRoadUser1.buildUnit());

        graph.addConnection(SE, SW);

        model.followPath(testRoadUser1, asPath(SW, SE, SW), hour(11));

        final TestRoadUser testRoadUser2 = new TestRoadUser(SW);
        model.register(testRoadUser2.buildUnit());
        model.followPath(testRoadUser2, asPath(SW, SE), hour(2));
        model.followPath(testRoadUser2, asPath(model.getPosition(testRoadUser1)), hour(2));
    }

    /**
     * Situation: <code>SW(tru2) ->- SE ->- tru1 ->- NE </code><br/>
     * tru2 moves towards tru1, it should end up between SE and tru1.
     */
    @Test
    public void followPathMoveTowardOther() {
        final TestRoadUser tru1 = new TestRoadUser(SW);
        model.register(tru1.buildUnit());
        model.followPath(tru1, asPath(SW, SE, NE), hour(12));

        final TestRoadUser tru2 = new TestRoadUser(SW);
        model.register(tru2.buildUnit());
        final MoveProgress pp = model.followPath(tru2, asPath(SW, SE, model
                .getPosition(tru1)), hour(11));
        assertEquals(11, pp.distance, EPSILON);

        final Point p1 = model.getPosition(tru1);
        final Point p2 = model.getPosition(tru2);

        final Point diff = Point.diff(p1, p2);
        assertTrue(diff.x == 0 && diff.y > 0);
    }

    /**
     * Situation: <code>SW ->- tru1 ->- SE </code><br/>
     * tru1 wants to move directly to SW, which should throw an exception since
     * moving backward over an directed edge is not allowed.
     */
    @Test(expected = IllegalArgumentException.class)
    public void followPathFailGoBackwardToNode() {
        final TestRoadUser testRoadUser1 = new TestRoadUser(SW);
        model.register(testRoadUser1.buildUnit());
        model.followPath(testRoadUser1, asPath(SW, SE), hour());
        model.followPath(testRoadUser1, asPath(SW), hour());
    }

    /**
     * Situation: <code>SW ->- tru1 ->- tru2 ->- SE </code><br/>
     * tru2 wants to move directly to tru1, which should throw an exception
     * since moving backward over an directed edge is not allowed.
     */
    @Test(expected = IllegalArgumentException.class)
    public void followPathFailGoBackwardToEdgePoint() {
        // first move tru1 and tru2 in position
        final TestRoadUser tru1 = new TestRoadUser(SW);
        model.register(tru1.buildUnit());
        model.followPath(tru1, asPath(SW, SE), hour());

        final TestRoadUser tru2 = new TestRoadUser(SW);
        model.register(tru2.buildUnit());
        model.followPath(tru2, asPath(SW, SE), hour(2));

        // now the road users are in the above described positions
        final Point difference = Point.diff(model.getPosition(tru1), model
                .getPosition(tru2));
        assertTrue(difference.x < 0 && difference.y == 0);

        model.followPath(tru2, asPath(model.getPosition(tru1)), hour(2));
    }

    /**
     * Situation: <code>SW ->- tru1 ->- SE -<- tru2 -<- NE </code><br/>
     * tru2 wants to move directly to tru1, which should throw an exception
     * since they are on different edges.
     */
    @Test(expected = IllegalArgumentException.class)
    public void followPathFailWrongEdge() {
        graph.addConnection(NE, SE);

        // first move tru1 and tru2 in position
        final TestRoadUser tru1 = new TestRoadUser(SW);
        model.register(tru1.buildUnit());
        model.followPath(tru1, asPath(SW, SE), hour());

        final TestRoadUser tru2 = new TestRoadUser(NE);
        model.register(tru2.buildUnit());
        model.followPath(tru2, asPath(NE, SE), hour(2));

        model.followPath(tru2, asPath(model.getPosition(tru1)), hour(2));
    }

    /**
     * Situation: <code>SW(tru1) ->- SE -<- tru2 -<- NE </code><br/>
     * tru2 wants to move directly to tru1, which should throw an exception
     * since they are on different edges.
     */
    @Test(expected = IllegalArgumentException.class)
    public void followPathFailWrongEdge2() {
        graph.addConnection(NE, SE);

        // first move tru1 and tru2 in position
        final TestRoadUser tru1 = new TestRoadUser(SW);
        model.register(tru1.buildUnit());

        final TestRoadUser tru2 = new TestRoadUser(NE);
        model.register(tru2.buildUnit());
        model.followPath(tru2, asPath(NE, SE), hour(2));

        model.followPath(tru1, asPath(model.getPosition(tru2)), hour(2));
    }

    /**
     * Situation: <code>SW(tru1) ->- SE -<- tru2 -<- NE </code><br/>
     * tru2 wants to move directly to tru1, which should throw an exception
     * since they are on different edges.
     */
    @Test(expected = IllegalArgumentException.class)
    public void followPathFailWrongEdgeIndirectly() {
        graph.addConnection(NE, SE);

        // first move tru1 and tru2 in position
        final TestRoadUser tru1 = new TestRoadUser(SW);
        model.register(tru1.buildUnit());

        final TestRoadUser tru2 = new TestRoadUser(NE);
        model.register(tru2.buildUnit());
        model.followPath(tru2, asPath(NE, SE), hour(2));

        model.followPath(tru1, asPath(SW, model.getPosition(tru2)), hour(200));
    }

    /**
     * Situation: <code>SW ->- tru1 ->- tru2 ->- SE</code><br/>
     * tru1 wants to move directly to tru2, which should be allowed.
     */
    @Test
    public void followPathChase() {
        // first move tru1 and tru2 in position
        final TestRoadUser tru1 = new TestRoadUser(SW);
        model.register(tru1.buildUnit());
        model.followPath(tru1, asPath(SW, SE), hour());

        final TestRoadUser tru2 = new TestRoadUser(SW);
        model.register(tru2.buildUnit());
        model.followPath(tru2, asPath(SW, SE), hour(2));

        model.followPath(tru1, asPath(model.getPosition(tru2)), hour(2));
        assertEquals(model.getPosition(tru1), model.getPosition(tru2));
    }

    /**
     * Situation: <code>SW(tru1) ->- tru2 ->- SE</code><br/>
     * tru1 wants to move directly to tru2, which should be allowed.
     */
    @Test
    public void followPathChaseFromNode() {
        // first move tru1 and tru2 in position
        final TestRoadUser tru1 = new TestRoadUser(SW);
        model.register(tru1.buildUnit());

        final TestRoadUser tru2 = new TestRoadUser(SW);
        model.register(tru2.buildUnit());
        model.followPath(tru2, asPath(SW, SE), hour(2));

        model.followPath(tru1, asPath(model.getPosition(tru2)), hour(3));
        assertEquals(model.getPosition(tru1), model.getPosition(tru2));
    }

    @Test
    public void moveTo() {
        final MovingRoadUser agent = new SpeedyRoadUser(1, SW);

        model.register(agent.buildUnit());
        assertEquals(SW, model.getPosition(agent));

        model.moveTo(agent, NW, hour(9));
        assertTrue(Point.distance(model.getPosition(agent), new Point(9, 0)) < EPSILON);

        model.followPath(agent, newLinkedList(asList(SE, NE)), hour(2));
        assertTrue(Point.distance(model.getPosition(agent), new Point(10, 1)) < EPSILON);

        model.moveTo(agent, NW, hour(2));
        assertTrue(Point.distance(model.getPosition(agent), new Point(10, 3)) < EPSILON);

        model.moveTo(agent, NW, hour(1));
        model.moveTo(agent, NE, hour(1));
        assertTrue(Point.distance(model.getPosition(agent), new Point(10, 5)) < EPSILON);

        model.followPath(agent, newLinkedList(asList(NE)), hour(2));
        assertTrue(Point.distance(model.getPosition(agent), new Point(10, 7)) < EPSILON);

        model.moveTo(agent, NW, hour(13));
        assertTrue(Point.distance(model.getPosition(agent), new Point(0, 10)) < EPSILON);

        final MovingRoadUser agent2 = new SpeedyRoadUser(1, SW);
        model.register(agent2.buildUnit());
        assertEquals(new Point(0, 0), model.getPosition(agent2));

        model.moveTo(agent2, agent, hour(30));
        assertEquals(model.getPosition(agent), model.getPosition(agent2));
    }

    @Test
    public void getConnectionTest() {
        final TestRoadUser driver = new TestRoadUser(SE);
        model.register(driver.buildUnit());
        assertNull(model.getConnection(driver));
        model.followPath(driver, asPath(NE), hour());
        assertTrue(connectionEquals(model.getConnection(driver), SE, NE));
    }

    @Test
    public void getShortestPath() {
        final TestRoadUser driver = new TestRoadUser(SW);
        model.register(driver.buildUnit());
        model.followPath(driver, asPath(SE), hour());
        assertTrue(connectionEquals(model.getConnection(driver), SW, SE));

        final List<Point> path = model.getShortestPathTo(driver, NE);
        assertEquals(asList(model.getPosition(driver), SE, NE), path);

    }

    @Test(expected = IllegalArgumentException.class)
    public void getShortestPathToFail() {
        model.getShortestPathTo(new TestRoadUser(), new Point(0, 0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void getShortestPathToFail2() {
        model.getShortestPathTo(new TestRoadUser(), new TestRoadUser());
    }

    @Test(expected = IllegalArgumentException.class)
    public void getShortestPathToFail3() {
        model.getShortestPathTo((RoadUser) null, new Point(0, 0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void getShortestPathToFail4() {
        model.getShortestPathTo((Point) null, new Point(0, 0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void getShortestPathToFail5() {
        model.getShortestPathTo(new Point(1, 2), null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getShortestPathToFail6() {
        model.getShortestPathTo((RoadUser) null, (RoadUser) null);
    }

    @Test(expected = NullPointerException.class)
    public void locationConstructorFail() {
        GraphRoadModel.newLoc(null);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void locationIsOnSameEdge() {
        ((Graph<MultiAttributeData>) graph)
                .addConnection(SE, SW, new MultiAttributeData(300));
        ((Graph<MultiAttributeData>) graph)
                .addConnection(NE, SW, new MultiAttributeData(Double.NaN));

        final Loc loc1 = GraphRoadModel.newLoc(new Connection<ConnectionData>(
                SW, SE, null), 3);
        final Loc loc2 = GraphRoadModel.newLoc(new Connection<ConnectionData>(
                SW, SE, null), 1);
        final Loc loc3 = GraphRoadModel.newLoc(new Connection<ConnectionData>(
                SE, NE, null), 9.999999);
        final Loc loc4 = GraphRoadModel.newLoc(SW);
        final Loc loc5 = GraphRoadModel.newLoc(new Connection<ConnectionData>(
                SE, SW, null), 1);
        final Loc loc6 = GraphRoadModel.newLoc(new Connection<ConnectionData>(
                NE, SW, null), 1);

        assertEquals(NE, loc3);
        assertTrue(loc1.isOnSameConnection(loc2));
        assertTrue(loc2.isOnSameConnection(loc1));
        assertFalse(loc1.isOnSameConnection(loc3));
        assertFalse(loc3.isOnSameConnection(loc1));
        assertFalse(loc4.isOnSameConnection(loc1));
        assertFalse(loc1.isOnSameConnection(loc4));
        assertFalse(loc3.isOnSameConnection(loc5));
        assertFalse(loc3.isOnSameConnection(loc6));
    }

    @Test(expected = NullPointerException.class)
    public void newLocationFail1() {
        GraphRoadModel.newLoc(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void newLocationFail2() {
        GraphRoadModel.newLoc(null, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void checkLocationFail1() {
        final Loc l = GraphRoadModel.newLoc(new Point(-10, -10));
        model.checkLocation(l);
    }

    @Test(expected = IllegalArgumentException.class)
    public void checkLocationFail2() {
        final Loc l = GraphRoadModel.newLoc(new Connection<ConnectionData>(
                new Point(-10, -10), new Point(100, 0), null), 1);
        model.checkLocation(l);
    }

    @Test
    public void getConnectionLength() {
        assertEquals(10, GraphRoadModel.getConnectionLength(new Connection<ConnectionData>(
                NE, NW, null)), EPSILON);
        final Connection<MultiAttributeData> conn = new Connection<MultiAttributeData>(
                NE, NW, new MultiAttributeData(12, -1));
        assertEquals(12, GraphRoadModel.getConnectionLength(conn), EPSILON);
        conn.getData()
                .put(MultiAttributeData.KEY_LENGTH, "this is not a number");
        assertEquals(10, GraphRoadModel.getConnectionLength(conn), EPSILON);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getMaxSpeedFail4() {
        model.getMaxSpeed(new TestRoadUser(), new Point(1, 2), new Point(2, 1));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void getMaxSpeed() {
        final Point A = new Point(0, 0);
        final Point B = new Point(10, 0);

        final Graph<LengthData> g = new MultimapGraph<LengthData>();
        final GraphRoadModel rm = new GraphRoadModel(g);
        g.addConnection(A, B, new LengthData(3));
        final SpeedyRoadUser speedy = new SpeedyRoadUser(10);
        rm.register(speedy.buildUnit());
        assertEquals(10, rm.getMaxSpeed(speedy, A, B), EPSILON);

        model.register(speedy.buildUnit());
        ((Graph<MultiAttributeData>) graph)
                .addConnection(SE, SW, new MultiAttributeData(3, 5));
        assertEquals(5, model.getMaxSpeed(speedy, SE, SW), EPSILON);

        ((Graph<MultiAttributeData>) graph)
                .addConnection(NE, SE, new MultiAttributeData(3, Double.NaN));
        assertEquals(10, model.getMaxSpeed(speedy, NE, SE), EPSILON);
    }

    @Test(expected = IllegalArgumentException.class)
    public void computeConnectionLengthFail1() {
        model.computeConnectionLength(null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void computeConnectionLengthFail2() {
        model.computeConnectionLength(new Point(1, 2), null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void computeConnectionLengthFail3() {
        model.computeConnectionLength(new Point(1, 2), new Point(2, 1));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void computeConnectionLength() {
        assertEquals(0, model.computeConnectionLength(new Point(1, 2), new Point(
                1, 2)), EPSILON);
        ((Graph<MultiAttributeData>) graph)
                .addConnection(SE, SW, new MultiAttributeData(5, 5));

        final TestRoadUser agent1 = new TestRoadUser(SE);
        model.register(agent1.buildUnit());
        final MoveProgress pp1 = model.followPath(agent1, asPath(SW), hour());
        pp1.toString();
        assertEquals(1, pp1.distance, EPSILON);
        assertEquals(hour().getTimeStep(), pp1.time);

        final TestRoadUser agent2 = new TestRoadUser(SE);
        model.register(agent2.buildUnit());
        final MoveProgress pp2 = model.followPath(agent2, asPath(SW), hour(2));
        assertEquals(2, pp2.distance, EPSILON);
        assertEquals(hour(2).getTimeStep(), pp2.time);

        assertEquals(1, model.computeConnectionLength(model.getPosition(agent1), model
                .getPosition(agent2)), EPSILON);

        assertEquals(4, model.computeConnectionLength(model.getPosition(agent1), SW), EPSILON);
        assertEquals(1, model.computeConnectionLength(SE, model
                .getPosition(agent1)), EPSILON);
    }

    @Test
    public void getGraphTest() {
        final Graph<ConnectionData> g = new MultimapGraph<ConnectionData>();
        g.addConnection(NE, SW);
        g.addConnection(SW, NW);

        final GraphRoadModel rm = new GraphRoadModel(g);
        assertEquals(g, rm.getGraph());
        g.addConnection(NE, NW);
        assertEquals(g, rm.getGraph());
    }

    @Test(expected = IllegalArgumentException.class)
    public void addTruckTest() {
        final RoadUser agent1 = new TestRoadUser();
        final RoadUser agent2 = new TestRoadUser(new Point(1,0));
        model.register(agent1.buildUnit());
        //model.register(agent2.buildUnit());// this location is not a
                                                   // crossroad
        model.addObjectAt(agent2, new Point(1,0));
    }

    // @Test
    // public void followPathHalfway2() {
    // RoadUser agent1 = new TestRoadUser();
    // model.register(agent1, SW);
    // assertTrue(model.containsObjectAt(agent1, SW));
    // assertFalse(model.containsObjectAt(agent1, SE));
    // assertFalse(model.containsObjectAt(null, null));
    // assertTrue(model.equalPosition(agent1, agent1));
    //
    // model.followPath(agent1, new LinkedList<Point>(asList(SW, SE)), 5);
    // assertEquals(new Point(5, 0), model.getPosition(agent1));
    //
    // RoadUser agent2 = new TestRoadUser();
    // model.registerSamePosition(agent2, agent1);
    // assertEquals(new Point(5, 0), model.getPosition(agent2));
    // assertTrue(model.equalPosition(agent1, agent2));
    // assertFalse(model.equalPosition(null, agent1));
    // assertFalse(model.equalPosition(agent1, null));
    //
    // // rs.followPath(agent2, new
    // LinkedList<Point>(rs.getShortestPathTo(agent2, agent1)), 5);
    // }
    // @Test
    // public void followPathHalfway2() {
    // RoadUser agent1 = new TestRoadUser();
    // model.register(agent1, SW);
    // assertTrue(model.containsObjectAt(agent1, SW));
    // assertFalse(model.containsObjectAt(agent1, SE));
    // assertFalse(model.containsObjectAt(null, null));
    // assertTrue(model.equalPosition(agent1, agent1));
    //
    // model.followPath(agent1, new LinkedList<Point>(asList(SW, SE)), 5);
    // assertEquals(new Point(5, 0), model.getPosition(agent1));
    //
    // RoadUser agent2 = new TestRoadUser();
    // model.registerSamePosition(agent2, agent1);
    // assertEquals(new Point(5, 0), model.getPosition(agent2));
    // assertTrue(model.equalPosition(agent1, agent2));
    // assertFalse(model.equalPosition(null, agent1));
    // assertFalse(model.equalPosition(agent1, null));
    //
    // // rs.followPath(agent2, new
    // LinkedList<Point>(rs.getShortestPathTo(agent2, agent1)), 5);
    // }

}
