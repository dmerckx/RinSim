/**
 * 
 */
package rinde.sim.ui.renderers;

import java.util.Set;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;

import rinde.sim.core.graph.Point;
import rinde.sim.core.model.ModelProvider;
import rinde.sim.core.model.pdp.users.DeliveryPoint;
import rinde.sim.core.model.pdp.users.PickupPoint;
import rinde.sim.core.model.pdp.users.Truck;
import rinde.sim.core.model.road.RoadModel;
import rinde.sim.core.model.road.users.RoadUser;

/**
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 * 
 */
public class PDPModelRenderer implements ModelRenderer {

	protected final static RGB GRAY = new RGB(80, 80, 80);
	protected final static RGB GREEN = new RGB(0, 255, 0);
	protected final static RGB ORANGE = new RGB(255, 160, 0);

	protected RoadModel rm;
	protected final UiSchema uiSchema;
	
	public PDPModelRenderer() {
		uiSchema = new UiSchema();
		uiSchema.add("truckEmpty", "/graphics/perspective/empty-truck-32.png");
		uiSchema.add("truckFull", "/graphics/perspective/semi-truck-32.png");
		uiSchema.add("pickup1", "/graphics/flat/person-black-32.png");
		uiSchema.add("pickup2", "/graphics/flat/person-blue-32.png");
		uiSchema.add("pickup3", "/graphics/flat/person-red-32.png");
		uiSchema.add("delivery", "/graphics/perspective/flag.png");
	}

	@Override
	public void renderStatic(GC gc, ViewPort vp) {}

	@Override
	public void renderDynamic(GC gc, ViewPort vp, long time) {
		final int radius = 4;
		final int outerRadius = 10;
		uiSchema.initialize(gc.getDevice());
		gc.setBackground(uiSchema.getDefaultColor());

		final Set<RoadUser<?>> objects = rm.getObjects();
		synchronized (objects) {
			for (final RoadUser<?> user : objects) {
				final Point p = user.getRoadState().getLocation();
				final Class<?> type = user.getClass();
				final int x = vp.toCoordX(p.x) - radius;
				final int y = vp.toCoordY(p.y) - radius;
				

				Image image = uiSchema.getImage(type);
				if(user instanceof Truck){
					image = uiSchema.getImage(((Truck) user).getContainerState().getLoad().size() == 0?"truckEmpty":"truckFull");
				}
				else if(user instanceof PickupPoint){
					switch(((PickupPoint) user).getPickupPointState().getPickupState()){
						case SETTING_UP: image = uiSchema.getImage("pickup1");
							break;
						case AVAILABLE: image = uiSchema.getImage("pickup2");
							break;
						case LATE: image = uiSchema.getImage("pickup3");
							break;
						default: continue;
					}
				}
				else if(user instanceof DeliveryPoint){
					switch(((DeliveryPoint) user).getDeliveryPointState().getDeliveryState()){
						case SETTING_UP: 
						case AVAILABLE:
						case LATE: 
						case BEING_DELIVERED:image = uiSchema.getImage("delivery");
							break;
						default: continue;
					}
				}

				if (image != null) {
					final int offsetX = x - image.getBounds().width / 2;
					final int offsetY = y - image.getBounds().height / 2;
					gc.drawImage(image, offsetX, offsetY);
				} else {
					final Color color = uiSchema.getColor(type);
					if (color == null) {
						continue;
					}
					gc.setBackground(color);
					gc.fillOval((int) (vp.origin.x + (p.x - vp.rect.min.x) * vp.scale) - radius, (int) (vp.origin.y + (p.y - vp.rect.min.y)
							* vp.scale)
							- radius, 2 * radius, 2 * radius);
				}

			}
		}
	}

	@Override
	public ViewRect getViewRect() {
		return null;
	}

	@Override
	public void registerModelProvider(ModelProvider mp) {
		rm = mp.getModel(RoadModel.class);
	}

	public Class<RoadModel> getSupportedModelType() {
		return RoadModel.class;
	}

}
