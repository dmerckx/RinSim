/**
 * 
 */
package rinde.sim.problem.fabrirecht;

import rinde.sim.core.model.pdp.scenario.PDPScenarioEvent;
import rinde.sim.scenario.TimedEvent;

/**
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 * 
 */
public class AddVehicleEvent extends TimedEvent {

	public final VehicleDTO vehicleDTO;

	public AddVehicleEvent(long time, VehicleDTO dto) {
		super(PDPScenarioEvent.ADD_VEHICLE, time);
		vehicleDTO = dto;
	}

}
