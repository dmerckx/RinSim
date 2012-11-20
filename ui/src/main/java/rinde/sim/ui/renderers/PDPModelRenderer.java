/**
 * 
 */
package rinde.sim.ui.renderers;

import java.util.Collection;
import java.util.Set;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.RGB;

import rinde.sim.core.graph.Point;
import rinde.sim.core.model.ModelProvider;
import rinde.sim.core.old.pdp.PDPModel;
import rinde.sim.core.old.pdp.Parcel_Old;
import rinde.sim.core.old.pdp.Vehicle_Old;
import rinde.sim.core.old.pdp.PDPModel.ParcelState;
import rinde.sim.core.old.pdp.PDPModel.VehicleState;

/**
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 * 
 */
public class PDPModelRenderer implements ModelRenderer {

	protected final static RGB GRAY = new RGB(80, 80, 80);
	protected final static RGB GREEN = new RGB(0, 255, 0);
	protected final static RGB ORANGE = new RGB(255, 160, 0);

	protected PDPModel pdpModel;

	@Override
	public void renderStatic(GC gc, ViewPort vp) {}

	@Override
	public void renderDynamic(GC gc, ViewPort vp, long time) {
		final Set<Vehicle_Old> vehicles = pdpModel.getVehicles();
		synchronized (vehicles) {
			for (final Vehicle_Old v : vehicles) {
				final Point p = pdpModel.getPosition(v);
				final double size = pdpModel.getContentsSize(v);

				final Collection<Parcel_Old> contents = pdpModel.getContents(v);
				final int x = vp.toCoordX(p.x);
				final int y = vp.toCoordY(p.y);
				gc.drawText("" + size, x, y);
				for (final Parcel_Old parcel : contents) {
					gc.drawLine(x, y, vp.toCoordX(parcel.getDestination().x), vp.toCoordY(parcel.getDestination().y));
				}
				final VehicleState state = pdpModel.getVehicleState(v);
				// FIXME, investigate why the second check is neccesary..
				if (state != VehicleState.IDLE && pdpModel.getVehicleActionInfo(v) != null) {
					gc.drawText(state.toString() + " " + pdpModel.getVehicleActionInfo(v).timeNeeded(), x, y - 20);
				}
			}
		}

		final Collection<Parcel_Old> parcels = pdpModel.getParcels(ParcelState.AVAILABLE, ParcelState.ANNOUNCED);
		synchronized (parcels) {
			for (final Parcel_Old parcel : parcels) {
				final Point p = pdpModel.getPosition(parcel);
				final int x = vp.toCoordX(p.x);
				final int y = vp.toCoordY(p.y);
				gc.drawLine(x, y, vp.toCoordX(parcel.getDestination().x), vp.toCoordY(parcel.getDestination().y));

				RGB color = null;
				if (pdpModel.getParcelState(parcel) == ParcelState.ANNOUNCED) {
					color = GRAY;
				} else if (parcel.getPickupTimeWindow().isIn(time)) {
					color = GREEN;
				} else {
					color = ORANGE;
				}
				gc.setBackground(new Color(gc.getDevice(), color));
				gc.fillOval(x - 5, y - 5, 10, 10);
			}
		}
	}

	@Override
	public ViewRect getViewRect() {
		return null;
	}

	@Override
	public void registerModelProvider(ModelProvider mp) {
		pdpModel = mp.getModel(PDPModel.class);
	}

	public Class<PDPModel> getSupportedModelType() {
		return PDPModel.class;
	}

}
