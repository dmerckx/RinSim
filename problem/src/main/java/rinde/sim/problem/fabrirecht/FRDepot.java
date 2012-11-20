/**
 * 
 */
package rinde.sim.problem.fabrirecht;

import rinde.sim.core.graph.Point;
import rinde.sim.core.model.road.RoadModel;
import rinde.sim.core.old.pdp.Depot_Old;
import rinde.sim.core.old.pdp.PDPModel;

/**
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 * 
 */
public class FRDepot extends Depot_Old {

	public FRDepot(Point startPosition) {
		setStartPosition(startPosition);
	}

	@Override
	public void initRoadPDP(RoadModel pRoadModel, PDPModel pPdpModel) {}

}
