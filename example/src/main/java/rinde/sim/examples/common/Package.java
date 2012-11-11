package rinde.sim.examples.common;

import rinde.sim.core.graph.Point;
import rinde.sim.core.model.road.RoadModel;
import rinde.sim.core.model.road.users.RoadUser;

public class Package implements RoadUser {
	public final String name;
	private Point location;

	public Package(String name, Point location) {
		this.name = name;
		this.location = location;
	}

	@Override
	public String toString() {
		return name;
	}

	@Override
	public void initRoadUser(RoadModel model) {
		model.addObjectAt(this, location);
	}

	public Point getLocation() {
		return location;
	}
}