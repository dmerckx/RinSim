package plots;

public class Standards {
	public static final double FIND_PACKAGE_RADIUS = 100;
	public static final double GRADIENT_RADIUS = 100;
	public static final double BROADCAST_RADIUS = 100;

	public static final double SPEED = 1;
	public static final double PROPORTION = 3;
	
	public static int getBlocks(int agents){
		return getBlocks(agents, (int) Standards.PROPORTION);
	}
	
	public static int getBlocks(int agents, int proportion){
		double a = agents * (4/ (proportion+1));
		
		return a < 70 ? 1 : (int) (0.90 * Math.sqrt(agents) -4);
	}
}
