package rinde.sim.core.simulation.types;

import rinde.sim.core.model.Model;
import rinde.sim.core.simulation.Simulator;
import rinde.sim.core.simulation.TickListener;
import rinde.sim.core.simulation.TimeInterval;

/**
 * This extension of the general {@link TickListener} can be used
 * {@link Model}s that are interested in the time progress
 * of the {@link Simulator}.
 * 
 * @author dmerckx
 */
public interface PrimaryTickListener extends TickListener<TimeInterval>{

}
