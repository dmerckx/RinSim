/**
 * 
 */
package rinde.sim.examples.pdp_old;

import rinde.sim.core.graph.Point;
import rinde.sim.core.model.road.RoadModel;
import rinde.sim.core.old.pdp.Depot_Old;
import rinde.sim.core.old.pdp.PDPModel;

/**
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 * 
 */
public class ExampleDepot extends Depot_Old {

	public ExampleDepot(Point position, double capacity) {
		setStartPosition(position);
		setCapacity(capacity);
	}

	@Override
	public void initRoadPDP(RoadModel pRoadModel, PDPModel pPdpModel) {}

}
