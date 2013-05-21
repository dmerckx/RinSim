package plots;

public class Standards {
	public static final double FIND_PACKAGE_RADIUS = 100;
	public static final double GRADIENT_RADIUS = 100;
	public static final double BROADCAST_RADIUS = 100;

	public static final double SPEED = 1;
	public static final double PROPORTION = 3;
	
	public static int getBlocks(int agents){
		return agents < 100 ? 0 : (int)(1.5 * Math.sqrt(agents));
	}
}
