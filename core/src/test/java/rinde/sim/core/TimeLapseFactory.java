package rinde.sim.core;

import rinde.sim.core.simulation.TimeLapse;

/**
 * @author Rinde van Lon (rinde.vanlon@cs.kuleuven.be)
 */
public final class TimeLapseFactory {

	private TimeLapseFactory() {}

	// this should only be used in tests!

	public static TimeLapse create(long start, long end) {
		return new TimeLapse(start, end);
	}

}
