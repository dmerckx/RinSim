package gradient.model.apis;


public abstract class GradientState {

	GradientState() {}
    
	public abstract double getStrength();
	
    public abstract boolean getIsActive();
}