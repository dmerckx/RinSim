package plots;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;

import rinde.sim.core.simulation.policies.AgentsPolicy;
import rinde.sim.core.simulation.policies.execution.ModPoolBatchRecursive;

import comparison.Scenario;
import comparison.Scenario.Result;

public class ScenariosTablesDetailed {
	private static final int TICKS = 1000;
	public static final int CORES = 2;
	
	public static final double SAMPLES = 15;
	
	public static final int[] AGENTS = new int[]{100, 200, 500, 1000};
	public static final int[] SPEED = new int[]{1, 2};
	public static final int[] PROPS = new int[]{1, 2, 3, 4, 5};
	
	public static final int COLUMNS = SPEED.length * PROPS.length;
	
	public static final String[] NAMES = new String[]{"naive", "gradie ntfield", "contractnet"};
	
	public static void main(String[] args) {
		for(int i = 0; i <= 2; i++){
			runScenario(i);
		}
	}
	
	public static void runScenario(int scenarioNr){
		int[][] results = getResults(scenarioNr);
		
		System.out.println(" ");
		
		String str = 
		//"\\begin{figure}\n"+
		//"  \\centering\n"+
		"    \\begin{tabular}{l"+rep("|r",COLUMNS)+"|}\n"+
		"    \\cline{2-"+(COLUMNS+1)+"}\n"+
		"    " + getSpeedHeading() + " \\\\\n"+ 
		"    \\cline{1-"+(COLUMNS+1)+"}\n"+
		"    \\multicolumn{1}{|c|}{agents} 	" + getPropsHeading() + "\\\\\n" +
		"    \\hline\n";
		
		for(int a = 0; a < AGENTS.length; a++){
			str += "    \\multicolumn{1}{|c|}{" + AGENTS[a] + "} ";
			
			for(int r:results[a]){
				str += " & " + r + " ";
			}
			str +=  "\\\\\n";
		}
		
		//"    \\multicolumn{1}{|c|}{100}    & 13.65 	& 13.65		& 13.65		& 33.33		\\\\\n"+
		
		
		str +=
		"    \\hline\n"+
		"    \\end{tabular}\n";
		//"  \\caption{TODO}\n"+
		//"  \\label{fig:"+NAMES[scenarioNr]+"}\n"+
		//"\\end{figure}\n\n";
		
		System.out.println(str);
		System.out.println(" ");
	}
	
	public static String rep(String base, int times){
		String result = "";
		for(int i = 0; i < times; i++) result += base;
		return result;
	}
	
	public static String getSpeedHeading(){
		String result = "";
		for(int s = 0; s < SPEED.length; s++)
			result += "& \\multicolumn{"+PROPS.length+"}{c|}{speed "+SPEED[s]+"}";
		return result;
	}
	
	public static String getPropsHeading(){
		String result = "";
		for(int s = 0; s < SPEED.length; s++)
			for(int p = 0; p < PROPS.length; p++)
				result += "& prop " + PROPS[p] + " ";
		return result;
	}
	
	public static int[][] getResults(int scenarioNr){
		int[][] result = new int[AGENTS.length][SPEED.length * PROPS.length];
		RandomGenerator rng = new MersenneTwister(15);
		
		System.out.println("Running " + NAMES[scenarioNr] + ":");
		for(int a = 0; a < AGENTS.length; a++){
			System.out.println(" for " + AGENTS[a] + " agents");
			
			for(int s = 0; s < SPEED.length; s++){
				for(int p = 0; p < PROPS.length; p++){
					int totalDeliveries = 0;
					System.out.print("s" + SPEED[s]+"p" + PROPS[p] + ":");
					for(int i = 0; i < SAMPLES; i++){
						System.out.print(".");
						
						Result r = run(scenarioNr, rng.nextInt(), SPEED[s], TICKS, AGENTS[a], PROPS[p]);
						totalDeliveries += r.deliveries;
					}
					System.out.println("");
					result[a][s * PROPS.length + p] = (int) (totalDeliveries / SAMPLES);
				}
			}
		}
		
		return result;
	}
	
	
	private static Result run(int scenarioNr, int seed, double speed, int ticks, int cars, double proportion){
		AgentsPolicy policy = new ModPoolBatchRecursive(5, 2);
		Scenario s = Scenario.makeScenario(scenarioNr, seed, policy, speed, ticks, cars, proportion,
				Standards.FIND_PACKAGE_RADIUS, Standards.GRADIENT_RADIUS, Standards.BROADCAST_RADIUS);
		s.init(Standards.getBlocks(cars));
		return s.run();
	}
}
