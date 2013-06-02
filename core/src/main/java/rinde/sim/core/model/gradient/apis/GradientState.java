package rinde.sim.core.model.gradient.apis;


public abstract class GradientState {

	GradientState() {}
    
	public abstract double getStrength();
	
    public abstract boolean getIsActive();
}