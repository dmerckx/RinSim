/**
 * 
 */
package rinde.sim.problem.fabrirecht;

import rinde.sim.core.model.road.RoadModel;
import rinde.sim.core.old.pdp.PDPModel;
import rinde.sim.core.old.pdp.Parcel_Old;

/**
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 * 
 */
public class FRParcel extends Parcel_Old {

	public final ParcelDTO dto;

	public FRParcel(ParcelDTO pDto) {
		super(pDto.destinationLocation, pDto.pickupDuration, pDto.pickupTimeWindow, pDto.deliveryDuration,
				pDto.deliveryTimeWindow, pDto.neededCapacity);
		setStartPosition(pDto.pickupLocation);
		dto = pDto;
	}

	@Override
	public void initRoadPDP(RoadModel pRoadModel, PDPModel pPdpModel) {}

}
