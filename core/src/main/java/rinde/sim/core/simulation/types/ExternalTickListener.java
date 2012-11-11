package rinde.sim.core.simulation.types;

import rinde.sim.core.simulation.Simulator;
import rinde.sim.core.simulation.TickListener;
import rinde.sim.core.simulation.TimeInterval;


/**
 * This extension of the general {@link TickListener} is used to
 * model any external classes interested in the time progress
 * of a {@link Simulator}.
 * 
 * Examples of external classes like GUI's and loggers.  
 * 
 * @author dmerckx
 */
public interface ExternalTickListener extends TickListener<TimeInterval>{

}
