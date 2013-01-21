package rinde.sim.examples.rwalk3;

import rinde.sim.core.graph.Point;
import rinde.sim.core.simulation.TimeLapse;
import rinde.sim.examples.common.MovingAgent;

/**
 * Example of a simple random agent, moving around in the simulator.
 */
public class RandomWalkAgent extends MovingAgent {

	@Override
	public void tick(TimeLapse timeLapse) {
		//Controleer of de agent nog aan het rijden is
		if(!roadAPI.isDriving()){ 
			//Vraag een nieuwe random locatie op
			Point newTarget = roadAPI.getRandomLocation();
			
			//Stel deze random locatie in als het nieuwe doel
			roadAPI.setTarget(newTarget); 
		}
		
		//Gebruik de gegeven tijd om zo ver mogelijk te rijden
		roadAPI.advance(timeLapse); 
	}
}
