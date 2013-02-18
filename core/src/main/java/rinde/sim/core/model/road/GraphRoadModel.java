/**
 * 
 */
package rinde.sim.core.model.road;

import static com.google.common.base.Preconditions.checkArgument;
import static rinde.sim.core.graph.Graphs.shortestPathEuclideanDistance;
import static rinde.sim.core.graph.Graphs.unmodifiableGraph;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;

import rinde.sim.core.graph.Connection;
import rinde.sim.core.graph.ConnectionData;
import rinde.sim.core.graph.Graph;
import rinde.sim.core.graph.MultiAttributeData;
import rinde.sim.core.graph.Point;
import rinde.sim.core.model.road.GraphRoadModel.Loc;
import rinde.sim.core.model.road.users.MovingRoadUser;
import rinde.sim.core.model.road.users.RoadUser;
import rinde.sim.core.simulation.TimeLapse;
import rinde.sim.util.SpeedConverter;
import rinde.sim.util.TimeUnit;

/**
 * A {@link RoadModel} that uses a {@link Graph} as road structure.
 * {@link RoadUser}s can only be added on nodes on the graph. This model assumes
 * that the {@link Graph} does <b>not</b> change. Modifying the graph after
 * passing it to the model may break this model. The graph can define
 * {@link Connection} specific speed limits using {@link MultiAttributeData}.
 * 
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 * @author Bartosz Michalik <bartosz.michalik@cs.kuleuven.be> changes wrt.
 *         models infrastructure
 */
public class GraphRoadModel extends AbstractRoadModel<Loc> {

    // FIXME precision stuff should be defined in the interface, implemented in
    // abstract class and thoroughly tested
    // TODO what about precision?
    protected static final double DELTA = 0.000001;

    /**
     * The graph that is used as road structure.
     */
    protected final Graph<? extends ConnectionData> graph;

    // TODO can null checks be removed now?

    /**
     * Creates a new instance using the specified {@link Graph} as road
     * structure.
     * @param pGraph The graph which will be used as road strucutre.
     */
    public GraphRoadModel(Graph<? extends ConnectionData> pGraph) {
        super();
        checkArgument(pGraph != null, "Graph can not be null");
        graph = pGraph;
    }

    @Override
    public void addObjectAt(RoadUser newObj, Point pos) {
        checkArgument(graph.containsNode(pos), "Object must be initiated on a crossroad.");
        super.addObjectAt(newObj, newLoc(pos));
    }

    // TODO add unit tests for timelapse inputs
    @Override
    protected MoveProgress doFollowPath(MovingRoadUser<?> object,
            Queue<Point> path, TimeLapse time) {
        // checkArgument(object != null, "object cannot be null");
        // checkArgument(objLocs.containsKey(object),
        // "object must have a location");
        // checkArgument(path.peek() != null, "path can not be empty");
        // checkArgument(time.hasTimeLeft(),
        // "can not follow path when to time is left");
        // // checkArgument(time > 0, "time must be a positive number");

        final Loc objLoc = objLocs.get(object);
        checkLocation(objLoc);

        // long timeLeft = time;
        double traveled = 0;

        Loc tempLoc = objLoc;
        Point tempPos = objLoc;

        double newDis = Double.NaN;

        final SpeedConverter sc = new SpeedConverter();

        final List<Point> travelledNodes = new ArrayList<Point>();
        while (time.hasTimeLeft() && path.size() > 0) {
            checkIsValidMove(tempLoc, path.peek());

            // speed in graph units per hour -> converting to miliseconds
            double speed = getMaxSpeed(object, tempPos, path.peek());
            speed = sc.from(speed, TimeUnit.H).to(TimeUnit.MS);

            // distance that can be traveled in current edge with timeleft
            final double travelDistance = speed * time.getTimeLeft();
            final double connLength = computeConnectionLength(tempPos, path.peek());

            if (travelDistance >= connLength) {
                // jump to next vertex
                tempPos = path.remove();
                if (!(tempPos instanceof Loc)) {
                    travelledNodes.add(tempPos);
                }
                final long timeSpent = Math.round(connLength / speed);
                time.consume(timeSpent);
                traveled += connLength;

                if (tempPos instanceof Loc) {
                    tempLoc = checkLocation((Loc) tempPos);
                } else {
                    tempLoc = checkLocation(newLoc(tempPos));
                }

            } else { // distanceLeft < connLength
                newDis = travelDistance;
                time.consumeAll();
                // timeLeft = 0;
                // long timeSpent = Math.round(travelDistance / speed);
                // timeLeft -= timeSpent;
                traveled += travelDistance;

                final Point from = isOnConnection(tempLoc) ? tempLoc.conn.from
                        : tempLoc;
                final Point peekTo = isOnConnection(path.peek()) ? ((Loc) path
                        .peek()).conn.to : path.peek();
                final Connection<?> conn = graph.getConnection(from, peekTo);
                tempLoc = checkLocation(newLoc(conn, tempLoc.relativePos
                        + newDis));
            }
            tempPos = tempLoc;
        }

        objLocs.put(object, tempLoc);
        return new MoveProgress(traveled, time.getTimeConsumed(),
                travelledNodes);
    }

    /**
     * Check if it is possible to move from <code>objLoc</code> to
     * <code>nextHop</code>.
     * @param objLoc The current location.
     * @param nextHop The destination node.
     */
    protected void checkIsValidMove(Loc objLoc, Point nextHop) {
        // in case we start from an edge and our next destination is to go to
        // the end of the current edge then its ok. Otherwise more checks are
        // required..
        
        if (objLoc.isOnConnection() && !nextHop.equals(objLoc.conn.to)) {
            // check if next destination is a MidPoint
            checkArgument(nextHop instanceof Loc, "Illegal path for this object, from a position on an edge we can not jump to another edge or go back.");
            final Loc dest = (Loc) nextHop;
            // check for same edge
            checkArgument(objLoc.isOnSameConnection(dest), "Illegal path for this object, first point is not on the same edge as the object.");
            // check for relative position
            checkArgument(objLoc.relativePos <= dest.relativePos, "Illegal path for this object, can not move backward over an edge.");
        }
        // in case we start from a node and we are not going to another node
        else if (!objLoc.isOnConnection() && !nextHop.equals(objLoc)
                && !graph.hasConnection(objLoc, nextHop)) {
            checkArgument(nextHop instanceof Loc, "Illegal path, first point should be directly connected to object location.");
            final Loc dest = (Loc) nextHop;
            checkArgument(graph.hasConnection(objLoc, dest.conn.to), "Illegal path, first point is on an edge not connected to object location. ");
            checkArgument(objLoc.equals(dest.conn.from), "Illegal path, first point is on a different edge.");
        }
    }

    /**
     * Compute length of connection as defined by the two points. If points are
     * equal the distance is 0. This method uses length stored in
     * {@link ConnectionData} objects when available.
     * @param from Start of the connection.
     * @param to End of the connection.
     * @return the distance between two points
     * @throws IllegalArgumentException when two points are part of the graph
     *             but are not equal or there is no connection between them
     */
    protected double computeConnectionLength(Point from, Point to) {
        if (from == null) {
            throw new IllegalArgumentException("from can not be null");
        }
        if (to == null) {
            throw new IllegalArgumentException("to can not be null");
        }
        if (from.equals(to)) {
            return 0;
        }
        if (isOnConnection(from) && isOnConnection(to)) {
            final Loc start = (Loc) from;
            final Loc end = (Loc) to;
            checkArgument(start.isOnSameConnection(end), "the points are not on the same connection");
            return Math.abs(start.relativePos - end.relativePos);
        } else if (isOnConnection(from)) {
            final Loc start = (Loc) from;
            checkArgument(start.conn.to.equals(to), "from is not on a connection leading to 'to'");
            return start.connLength - start.relativePos;
        } else if (isOnConnection(to)) {
            final Loc end = (Loc) to;
            checkArgument(end.conn.from.equals(from), "to is not connected to from");
            return end.relativePos;
        } else {
            checkArgument(graph.hasConnection(from, to), "connection does not exist");
            return getConnectionLength(graph.getConnection(from, to));
        }
    }

    /**
     * Retrieves the length of the specified connection if it is defined.
     * @param conn The connection to check.
     * @return The length.
     */
    protected static double getConnectionLength(Connection<?> conn) {
        return conn.getData() == null
                || Double.isNaN(conn.getData().getLength()) ? Point
                .distance(conn.from, conn.to) : conn.getData().getLength();
    }

    /**
     * Checks if the point is on a connection.
     * @param p The point to check.
     * @return <code>true</code> if the point is on a connection,
     *         <code>false</code> otherwise.
     */
    protected static boolean isOnConnection(Point p) {
        return p instanceof Loc && ((Loc) p).isOnConnection();
    }

    /**
     * Checks whether the specified location is valid.
     * @param l The location to check.
     * @return The location if it is valid.
     * @throws IllegalArgumentException if the location is not valid.
     */
    protected Loc checkLocation(Loc l) {
        if(!(l.isOnConnection() || graph.containsNode(l)))
            throw new IllegalArgumentException("Location points to non-existing vertex: " + l + ".");
        if(!(!l.isOnConnection() || graph.hasConnection(l.conn.from, l.conn.to)))
            throw new IllegalArgumentException("Location points to non-existing connection: " + l.conn + ".");
        
        return l;
    }

    /**
     * Compute speed of the object taking into account the speed limits of the
     * object.
     * @param object traveling object
     * @param from the point on the graph object is located
     * @param to the next point on the path it want to reach
     * @return The maximum speed.
     */
    protected double getMaxSpeed(MovingRoadUser object, Point from, Point to) {
        if (!from.equals(to)) {
            final Connection<?> conn = getConnection(from, to);
            if (conn.getData() instanceof MultiAttributeData) {
                final MultiAttributeData maed = (MultiAttributeData) conn
                        .getData();
                final double speed = maed.getMaxSpeed();
                return Double.isNaN(speed) ? getSpeed(object) : Math
                        .min(speed, getSpeed(object));
            }
        }
        return getSpeed(object);
    }

    /**
     * Precondition: the specified {@link Point}s are part of a
     * {@link Connection} which exists in the {@link Graph}. This method figures
     * out which {@link Connection} the two {@link Point}s share.
     * @param from The start point.
     * @param to The end point.
     * @return The {@link Connection} shared by the points.
     */
    protected Connection<?> getConnection(Point from, Point to) {
        final boolean fromIsOnConn = isOnConnection(from);
        final boolean toIsOnConn = isOnConnection(to);
        Connection<?> conn = null;
        if (fromIsOnConn) {
            final Loc start = (Loc) from;
            if (toIsOnConn) {
                checkArgument(start.isOnSameConnection((Loc) to), "the specified points must be part of the same connection");
            } else {
                checkArgument(start.conn.to.equals(to), "the specified points must be part of the same connection");
            }
            conn = start.conn;

        } else if (toIsOnConn) {
            final Loc end = (Loc) to;
            checkArgument(end.conn.from.equals(from), "the specified points must be part of the same connection");
            conn = end.conn;
        } else {
            checkArgument(graph.hasConnection(from, to), "the specified points must be part of an existing connection in the graph");
            conn = graph.getConnection(from, to);
        }
        return conn;
    }

    @Override
    public List<Point> getShortestPathTo(Point from, Point to) {
        if (from == null) {
            throw new IllegalArgumentException("from can not be null");
        }
        if (to == null) {
            throw new IllegalArgumentException("to can not be null");
        }
        final List<Point> path = new ArrayList<Point>();
        Point start = from;
        if (isOnConnection(from)) {
            start = ((Loc) from).conn.to;
            path.add(from);
        }

        Point end = to;
        if (isOnConnection(to)) {
            end = ((Loc) to).conn.from;
        }
        path.addAll(doGetShortestPathTo(start, end));
        if (isOnConnection(to)) {
            path.add(to);
        }
        return path;
    }

    /**
     * Uses the A* algorithm:
     * {@link rinde.sim.core.graph.Graphs#shortestPathEuclideanDistance}. This
     * method can optionally be overridden by subclasses to define another
     * shortest path algorithm.
     * @param from The start point of the path.
     * @param to The end point of the path.
     * @return The shortest path.
     */
    protected List<Point> doGetShortestPathTo(Point from, Point to) {
        return shortestPathEuclideanDistance(graph, from, to);
    }

    /**
     * @return An unmodifiable view on the graph.
     */
    public Graph<? extends ConnectionData> getGraph() {
        return unmodifiableGraph(graph);
    }

    /**
     * Retrieves the connection which the specified {@link RoadUser} is at. If
     * the road user is at a vertex <code>null</code> is returned instead.
     * @param obj The object which position is checked.
     * @return A {@link Connection} if <code>obj</code> is on one,
     *         <code>null</code> otherwise.
     */
    public Connection<? extends ConnectionData> getConnection(RoadUser obj) {
        final Loc point = objLocs.get(obj);
        if (isOnConnection(point)) {
            return graph.getConnection(point.conn.from, point.conn.to);
        }
        return null;
    }

    /**
     * Creates a new {@link Loc} based on the provided {@link Point}.
     * @param p The point used as input.
     * @return A {@link Loc} with identical position as the specified
     *         {@link Point}.
     */
    @SuppressWarnings("synthetic-access")
    protected static Loc newLoc(Point p) {
        return new Loc(p.x, p.y, null, -1, 0);
    }

    /**
     * Creates a new {@link Loc} based on the provided {@link Connection} and
     * the relative position. The new {@link Loc} will be placed on the
     * connection with a distance of <code>relativePos</code> to the start of
     * the connection.
     * @param conn The {@link Connection} to use.
     * @param relativePos The relative position measured from the start of the
     *            {@link Connection}.
     * @return A new {@link Loc}
     */
    @SuppressWarnings("synthetic-access")
    protected static Loc newLoc(Connection<? extends ConnectionData> conn,
            double relativePos) {
        if (conn == null) {
            throw new IllegalArgumentException("conn can not be null");
        }
        final Point diff = Point.diff(conn.to, conn.from);
        final double roadLength = getConnectionLength(conn);

        final double perc = relativePos / roadLength;
        if (perc + DELTA >= 1) {
            return new Loc(conn.to.x, conn.to.y, null, -1, 0);
        }
        return new Loc(conn.from.x + perc * diff.x,
                conn.from.y + perc * diff.y, conn, roadLength, relativePos);
    }

    @Override
    protected Point locObj2point(Loc locObj) {
        return locObj;
    }

    @Override
    protected Loc point2LocObj(Point point) {
        return newLoc(point);
    }

    @Override
    public Point getRandomPosition(RandomGenerator rnd) {
        return graph.getRandomNode(rnd);
    }

    /**
     * Location representation in a {@link Graph} for the {@link GraphRoadModel}
     * .
     * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
     */
    final protected static class Loc extends Point {
        private static final long serialVersionUID = 7070585967590832300L;
        /**
         * The length of the current connection.
         */
        public final double connLength;
        /**
         * The relative position of this instance compared to the start of the
         * connection.
         */
        public final double relativePos;
        /**
         * The {@link Connection} which this position is on, can be
         * <code>null</code>.
         */
        public final Connection<? extends ConnectionData> conn;

        private Loc(double pX, double pY,
                Connection<? extends ConnectionData> pConn, double pConnLength,
                double pRelativePos) {
            super(pX, pY);
            connLength = pConnLength;
            relativePos = pRelativePos;
            conn = pConn;
        }

        /**
         * @return <code>true</code> if the position is on a connection.
         */
        public boolean isOnConnection() {
            return conn != null;
        }

        /**
         * Check if this position is on the same connection as the provided
         * location.
         * @param l The location to compare with.
         * @return <code>true</code> if both {@link Loc}s are on the same
         *         connection, <code>false</code> otherwise.
         */
        public boolean isOnSameConnection(Loc l) {
            if (!isOnConnection() || !l.isOnConnection()) {
                return false;
            }
            return conn.equals(l.conn);
        }

        @Override
        public String toString() {
            return super.toString() + "{" + conn + "}";
        }
    }
}
