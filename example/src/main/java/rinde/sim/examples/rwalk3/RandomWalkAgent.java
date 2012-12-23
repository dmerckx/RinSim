package rinde.sim.examples.rwalk3;

import java.util.LinkedList;
import java.util.Queue;

import org.apache.commons.math3.random.RandomGenerator;

import rinde.sim.core.graph.Point;
import rinde.sim.core.model.Agent;
import rinde.sim.core.model.road.RoadModel;
import rinde.sim.core.model.road.apis.MovingRoadAPI;
import rinde.sim.core.model.road.apis.MovingRoadGuard;
import rinde.sim.core.model.road.apis.RoadState;
import rinde.sim.core.model.road.users.MovingRoadData;
import rinde.sim.core.model.road.users.MovingRoadUser;
import rinde.sim.core.model.road.users.RoadUser;
import rinde.sim.core.model.simulator.apis.SimulatorAPI;
import rinde.sim.core.model.simulator.users.SimulatorUser;
import rinde.sim.core.simulation.TimeLapse;
import rinde.sim.examples.common.MovingAgent;
import rinde.sim.examples.common.Package;

/**
 * Example of a simple random agent, moving around in the simulator.
 * @author Bartosz Michalik <bartosz.michalik@cs.kuleuven.be>
 */
public class RandomWalkAgent extends MovingAgent {


	public RandomWalkAgent() {
		
	}

	@Override
	public void tick(TimeLapse timeLapse) {
		if(!roadAPI.isDriving()){
			Point newTarget = roadAPI.getRandomLocation();
			roadAPI.setTarget(newTarget);
		}
		
		roadAPI.advance(timeLapse);
	}
}
