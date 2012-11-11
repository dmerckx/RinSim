package rinde.sim.examples.rwalk3;

import java.util.LinkedList;
import java.util.Queue;

import org.apache.commons.math3.random.RandomGenerator;

import rinde.sim.core.graph.Point;
import rinde.sim.core.model.road.RoadModel;
import rinde.sim.core.model.road.users.MovingRoadUser;
import rinde.sim.core.model.road.users.RoadUser;
import rinde.sim.core.model.simulator.SimulatorAPI;
import rinde.sim.core.model.simulator.SimulatorUser;
import rinde.sim.core.simulation.time.TimeLapse;
import rinde.sim.core.simulation.types.Agent;
import rinde.sim.examples.common.Package;

/**
 * Example of the simple random agent with the use of simulation facilities.
 * @author Bartosz Michalik <bartosz.michalik@cs.kuleuven.be>
 */
public class RandomWalkAgent implements Agent, MovingRoadUser, SimulatorUser {

	protected RoadModel rs;
	protected RoadUser currentPackage;
	protected Queue<Point> path;
	protected RandomGenerator rnd;
	private SimulatorAPI simulator;
	private final double speed;

	/**
	 * Create simple agent.
	 * @param speed default speed of object in graph units per millisecond
	 */
	public RandomWalkAgent(double speed) {
		this.speed = speed;
	}

	@Override
	public void tick(TimeLapse timeLapse) {
		if (path == null || path.isEmpty()) {
			if (currentPackage != null && rs.containsObject(currentPackage)) {
				rs.removeObject(currentPackage);
			}

			Point destination = rs.getRandomPosition(rnd);
			currentPackage = new Package("dummy package", destination);
			simulator.register(currentPackage);
			path = new LinkedList<Point>(rs.getShortestPathTo(this, destination));
		} else {
			rs.followPath(this, path, timeLapse);
		}

	}

	@Override
	public void initRoadUser(RoadModel model) {
		rs = model;
		Point pos = rs.getRandomPosition(rnd);
		rs.addObjectAt(this, pos);
	}

	@Override
	public void setSimulator(SimulatorAPI api) {
		simulator = api;
	}

	@Override
	public double getSpeed() {
		return speed;
	}

	@Override
	public void afterTick(TimeLapse timeLapse) {
		// empty on purpose
	}
}
