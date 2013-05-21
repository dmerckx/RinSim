package plots;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;

import rinde.sim.core.simulation.policies.AgentsPolicy;
import rinde.sim.core.simulation.policies.agents.SingleThreaded;

import comparison.Scenario;
import comparison.Scenario.Result;

public class BlockSizeTable {
	private static final int TICKS = 1000;
	
	public static final int CORES = 2;
	
	public static final double SAMPLES = 15;
	
	public static final int[] AGENTS = new int[]{100, 200, 500, 1000, 2000};
	public static final String[] NAMES = new String[]{"Naive", "GradientField", "ContractNet"};
	
	public static void main(String[] args) {
		runScenario();
	}
	
	public static void runScenario(){
		int[][] results = new int[3][AGENTS.length];
				
		for(int s = 0; s < 3; s++){
			results[s] = getResults(s);
		}
		
		System.out.println("    ");
		System.out.println("    ");
		
		String str = 
		"\\begin{figure}\n"+
		"  \\centering\n"+
		"    \\begin{tabular}{|l|| " + rep("r|", AGENTS.length) + "}\n"+
		"    \\hline \n"+
		"    Agents " + getAgentsHeader() +" \\\\\n"+ 
		"    \\hline\n"+
		"    \\hline\n";

		for(int s = 0; s < 3; s++){
			str += "	" + NAMES[s] + " ";
			for(int a = 0; a < AGENTS.length; a++){
				str += " & " + results[s][a] + " ";
			}
			str +=  "\\\\\n";
		}
		
		str +=
		"    \\hline\n"+
		"    \\end{tabular}\n"+
		"  \\caption{TODO}\n"+
		"  \\label{fig:comparisonmethods}\n"+
		"\\end{figure}\n\n";
		
		System.out.println(str);
	}
	
	private static String getAgentsHeader() {
		String result = "";
		for(int a = 0; a < AGENTS.length; a++){
			result += " & " + AGENTS[a] + " ";
		}
		return result;
	}

	public static int[] getResults(int scenarioNr){
		int[] result = new int[AGENTS.length];
		RandomGenerator rng = new MersenneTwister(15);
		
		for(int a = 0; a < AGENTS.length; a++){
			System.out.print(scenarioNr + " : " + AGENTS[a] + " agents ");
			int totalDeliveries = 0;
			for(int i = 0; i < SAMPLES; i++){
				Result r = run(scenarioNr, rng.nextInt(), Standards.SPEED, TICKS, AGENTS[a], Standards.PROPORTION);
				totalDeliveries += r.deliveries;
				System.out.print(".");
			}
			System.out.println("");
			result[a] = (int) (totalDeliveries / SAMPLES);
		}
		
		return result;
	}
	
	public static String rep(String base, int times){
		String result = "";
		for(int i = 0; i < times; i++) result += base;
		return result;
	}
	
	private static Result run(int scenarioNr, int seed, double speed, int ticks, int cars, double proportion){
		AgentsPolicy policy = new SingleThreaded();
		Scenario s = Scenario.makeScenario(scenarioNr, seed, policy, speed, ticks, cars, proportion,
				Standards.FIND_PACKAGE_RADIUS, Standards.GRADIENT_RADIUS, Standards.BROADCAST_RADIUS);
		s.init(cars < 100? 0 : (int) (1.5 * Math.sqrt(cars)));
		return s.run();
	}
}