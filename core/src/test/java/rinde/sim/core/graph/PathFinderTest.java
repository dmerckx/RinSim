/**
 * 
 */
package rinde.sim.core.graph;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static rinde.sim.core.graph.Graphs.findClosestObjects;
import static rinde.sim.core.graph.Graphs.pathLength;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import rinde.sim.core.TimeLapseFactory;
import rinde.sim.core.model.road.GraphRoadModel;
import rinde.sim.core.model.road.MoveProgress;
import rinde.sim.core.model.road.RoadModel;
import rinde.sim.core.model.road.RoadModels;
import rinde.sim.core.model.road.dummies.TrivialRoadUser;
import rinde.sim.core.model.road.supported.RoadUnit;
import rinde.sim.core.model.road.users.MovingRoadUser;
import rinde.sim.core.model.road.users.RoadUser;
import rinde.sim.util.TimeUnit;

import com.google.common.base.Predicate;

/**
 * @author Rinde van Lon (rinde.vanlon@cs.kuleuven.be) TODO this class should
 *         only be testing Graph classes. RoadModel related stuff should be
 *         splitted
 */
@RunWith(Parameterized.class)
public class PathFinderTest {

	@Parameters
	public static Collection<Object[]> configs() {
		return Arrays.asList(new Object[][] { { TestMultimapGraph.class }, { TestTableGraph.class } });
	}

	public PathFinderTest(Class<? extends Graph<LengthData>> clazz) {
		rmType = clazz;
	}

	Class<? extends Graph<LengthData>> rmType;

	private final double EPSILON = 0.02;

	RoadModel rm;
	Graph<?> graph;
	Point a, b, c, d, e, f, g;

	RoadUser o1, o2, o3, o4, o5, o6;
	List<RoadUser> allObjects;

	@Before
	public void setUp() throws InstantiationException, IllegalAccessException {
		graph = rmType.newInstance();
		rm = new GraphRoadModel(graph);

		a = new Point(0, 0);
		b = new Point(10, 0);
		c = new Point(15, 15);
		d = new Point(15, 20);
		e = new Point(22, 0);
		f = new Point(8, 20);
		g = new Point(0, 12);

		graph.addConnection(a, b);
		graph.addConnection(a, c);
		// rs.addConnection(a, d);

		graph.addConnection(b, a);
		graph.addConnection(b, c);

		graph.addConnection(c, d);
		graph.addConnection(c, e);

		graph.addConnection(d, c);
		graph.addConnection(d, f);

		graph.addConnection(e, b);

		graph.addConnection(f, g);

		graph.addConnection(g, a);

		o1 = new StringRoadUser("object1", a);
		o2 = new StringRoadUser("object2", b);
		o3 = new StringRoadUser("object3", c);
		o4 = new LongRoadUser(444L, d);
		o5 = new LongRoadUser(555L, e);
		o6 = new LongRoadUser(666L, f);
		allObjects = Arrays.asList(o1, o2, o3, o4, o5, o6);

		rm.register(o1.buildUnit());
		rm.register(o2.buildUnit());
		rm.register(o3.buildUnit());
		rm.register(o4.buildUnit());
		rm.register(o5.buildUnit());
		rm.register(o6.buildUnit());

	}

	class StringRoadUser extends TrivialRoadUser {
		public final String name;

		public StringRoadUser(String pName) {
			name = pName;
		}

		public StringRoadUser(String pName, Point pos) {
            super(pos);
		    name = pName;
        }
		
		@Override
		public String toString() {
			return name;
		}

	}

	class LongRoadUser extends TrivialRoadUser {
		public final long number;

		public LongRoadUser(long pNumber) {
			number = pNumber;
		}

        public LongRoadUser(long pNumber, Point pos) {
            super(pos);
            number = pNumber;
        }

		@Override
		public String toString() {
			return "" + number;
		}
	}

	@Test
	public void shortestDistance() {
		List<Point> t = Graphs.shortestPathEuclideanDistance(graph, a, d);
		compatibilityCheck(t);
		assertEquals(asList(a, c, d), t);

		List<Point> t2 = Graphs.shortestPathEuclideanDistance(graph, d, a);
		compatibilityCheck(t2);
		assertEquals(asList(d, f, g, a), t2);

		List<Point> t3 = Graphs.shortestPathEuclideanDistance(graph, g, e);
		compatibilityCheck(t3);
		assertEquals(asList(g, a, c, e), t3);

		List<Point> t4 = Graphs.shortestPathEuclideanDistance(graph, a, e);
		compatibilityCheck(t4);
		assertEquals(asList(a, c, e), t4);

		List<Point> t5 = Graphs.shortestPathEuclideanDistance(graph, a, c);
		compatibilityCheck(t5);
		assertEquals(asList(a, c), t5);

		List<Point> t6 = Graphs.shortestPathEuclideanDistance(graph, e, g);
		compatibilityCheck(t6);
		assertEquals(asList(e, b, c, d, f, g), t6);
	}

	@Test(expected = RuntimeException.class)
	public void impossiblePath() throws InstantiationException, IllegalAccessException {
		Graph<?> gg = rmType.newInstance();
		GraphRoadModel roads = new GraphRoadModel(gg);
		gg.addConnection(a, b);
		gg.addConnection(b, c);

		Graphs.shortestPathEuclideanDistance(roads.getGraph(), b, a);
	}

	public void compatibilityCheck(List<Point> t) {
		MovingRoadUser truck = new TrivialRoadUser(t.get(0));
		rm.register(truck.buildUnit());
		double len = pathLength(t);
		// speed of trivial truck is 1 len per hour thus we need to travel 'len'
		// hours
		MoveProgress progress = rm.followPath(truck, new LinkedList<Point>(t), TimeLapseFactory.create(0, TimeUnit.H
				.toMs((long) Math.ceil(len))));
		assertEquals(len, progress.distance, EPSILON);
	}

	@Test
	public void checkRutgerBug() throws InstantiationException, IllegalAccessException {
		Graph<?> g = rmType.newInstance();
		Point q = new Point(0, 10);
		Point r = new Point(10, 15);
		Point s = new Point(10, 5);
		Point t = new Point(20, 10);

		g.addConnection(q, r);
		g.addConnection(q, s);
		g.addConnection(s, t);

		// DotExporter.saveToDot(graph.getGraph(), "files/test/rutgerbug");

		// this shouldn't fail
		Graphs.shortestPathEuclideanDistance(g, q, t);
	}

	@Test
	public void findClosestObjectTest() {
		// actually, o1 and o2 have the same distance to ref (but o1 is added
		// earlier)
		assertEquals(o1, RoadModels.findClosestObject(new Point(5, 5), rm));
		assertEquals(o2, RoadModels.findClosestObject(new Point(5.000001, 5), rm));

		assertEquals(o6, RoadModels.findClosestObject(new Point(5, 5), rm, LongRoadUser.class));

		assertEquals(null, RoadModels.findClosestObject(new Point(5, 5), rm, new Predicate<RoadUser>() {
			@Override
			public boolean apply(RoadUser input) {
				return false;
			}
		}));
	}

	@Test
	public void findClosestObjectsTest() {
		assertEquals(Arrays.asList(o1, o2, o3, o6, o5, o4), RoadModels.findClosestObjects(new Point(5, 5), rm));
		assertEquals(Arrays.asList(o1, o2, o3), RoadModels.findClosestObjects(new Point(5, 5), rm, 3));
		assertEquals(Arrays.asList(o6, o5, o4), RoadModels.findClosestObjects(new Point(5, 5), rm, LongRoadUser.class, 300));
		assertEquals(Arrays.asList(), RoadModels.findClosestObjects(new Point(5, 5), rm, EmptyRoadUser.class, 1));

		assertEquals(Arrays.asList(o3, o6, o4, o5), RoadModels.findClosestObjects(new Point(8, 8), rm, new Predicate<RoadUser>() {
			@Override
			public boolean apply(RoadUser input) {
				return input instanceof LongRoadUser || rm.getPosition(input).equals(new Point(15, 15));
			}
		}, 99));
	}

	@Test(expected = IllegalArgumentException.class)
	public void findClosestObjectsTestFail1() {
		RoadModels.findClosestObjects(null, rm, 1);
	}

	@Test(expected = IllegalArgumentException.class)
	public void findClosestObjectsTestFail2() {
		findClosestObjects(new Point(5, 5), (Collection<?>) null, null, 1);
	}

	@Test(expected = IllegalArgumentException.class)
	public void findClosestObjectsTestFail3() {
		findClosestObjects(new Point(5, 5), asList(new Object()), null, 1);
	}

	@Test(expected = IllegalArgumentException.class)
	public void findClosestObjectsTestFail4() {
		RoadModels.findClosestObjects(new Point(5, 5), rm, 0);
	}

	@Test(expected = IllegalArgumentException.class)
	public void findClosestObjectsTestFail6() {
		RoadModels.findClosestObjects(new Point(5, 5), null, (Collection<EmptyRoadUser>) null, 1);
	}

	@Test
	public void findObjectsWithinRadiusTest() {
		Point ref = new Point(10, 10);
		assertArrayEquals(asList(o1, o2, o3, o4, o6).toArray(), RoadModels.findObjectsWithinRadius(ref, rm, 15)
				.toArray());
		assertArrayEquals(asList(o3).toArray(), RoadModels.findObjectsWithinRadius(ref, rm, 10).toArray());
		assertArrayEquals(asList(o2, o3).toArray(), RoadModels.findObjectsWithinRadius(ref, rm, 10.000000001).toArray());
		assertArrayEquals(asList().toArray(), RoadModels.findObjectsWithinRadius(ref, rm, 5).toArray());

		assertArrayEquals(asList(o1, o2, o3).toArray(), RoadModels
				.findObjectsWithinRadius(ref, rm, 15, StringRoadUser.class).toArray());
		assertArrayEquals(asList(o3).toArray(), RoadModels.findObjectsWithinRadius(ref, rm, 10, StringRoadUser.class)
				.toArray());
		assertArrayEquals(allObjects.toArray(), RoadModels.findObjectsWithinRadius(ref, rm, 30, RoadUser.class)
				.toArray());
		assertArrayEquals(asList().toArray(), RoadModels.findObjectsWithinRadius(ref, rm, 30, EmptyRoadUser.class)
				.toArray());

	}

	class EmptyRoadUser implements RoadUser {
        @Override
        public RoadUnit buildUnit() {
            throw new RuntimeException("NOT meant to be executed");
        }
	}
}
