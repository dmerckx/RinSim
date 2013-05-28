package blocksize;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;

import plots.Standards;
import rinde.sim.core.simulation.policies.AgentsPolicy;
import rinde.sim.core.simulation.policies.agents.SingleThreaded;

import comparison.Scenario;
import comparison.Scenario.Result;

public class BlockSizeTable {
	
	public static final int CORES = 2;
	
	public static final double SAMPLES = 1; //8;
	
	public static int[] AGENTS;
	public static int[] TICKS;
	
	private static final int AMOUNT = 11;
	private static final int BASE_TICKS = /*500 * 100; */2500 * 100;
	
	public static final int[] BLOCKSIZES = new int[]{0, 5, 10, 15, 20, 25, 30, 35, 45};
	public static final String[] NAMES = new String[]{"Naive", "GradientField", "ContractNet"};
	
	private static final RandomGenerator rng = new MersenneTwister();
	
	public static void main(String[] args) {
		AGENTS = new int[AMOUNT];
		TICKS = new int[AMOUNT];
		for(int i = 0; i < AMOUNT; i++){
			AGENTS[i] = (int) Math.pow(/*5*/ 10 + i*5, 2);
			TICKS[i] = BASE_TICKS / AGENTS[i];
		}
		
		warmup();
		runScenario();
	}
	
	public static void runScenario(){
		long[][] results = getResults();
		
		System.out.println("    ");
		System.out.println("    ");
		
		String str = 
		"\\begin{tabular}{l l |" + rep("r|", BLOCKSIZES.length) + "}\n"+
		"\\cline{3-"+(BLOCKSIZES.length+2)+"}\n"+
		"& & \\multicolumn{"+BLOCKSIZES.length+"}{c|}{blocksize} \\\\\n"+
		"\\hline \n"+
		"\\multicolumn{1}{|c|}{Agents} & \\multicolumn{1}{c||}{Ticks} " + getBlocksizesHeader() +" \\\\\n"+ 
		"\\hline\n";

		for(int a = 0; a < AGENTS.length; a++){
			str += "\\multicolumn{1}{|c|}{"+AGENTS[a]+"} & \\multicolumn{1}{c||}{"+TICKS[a]+"}";
			for(int b = 0; b < BLOCKSIZES.length; b++){
				
				for(int b2 = 0; b2 < BLOCKSIZES.length; b2++){
					if(results[a][b] > results[a][b2]){
						str += " & " + (results[a][b] == Long.MAX_VALUE? " ":results[a][b]) + " ";
						break;
					}
					if(b2 == (BLOCKSIZES.length - 1)){
						str += " & \\textbf{" + results[a][b] + "} ";
					}
				}
				
			}
			str +=  "\\\\\n";
		}
		
		str +=
		"\\hline\n"+
		"\\end{tabular}\n\n";
		
		System.out.println(str);
	}
	
	private static String getBlocksizesHeader() {
		String result = " & 1";
		for(int b = 1; b < BLOCKSIZES.length; b++){
			result += " & " + BLOCKSIZES[b];
		}
		return result;
	}
	
	private static void warmup(){
		Scenario s = getScenario(2000, 200);
		s.init(0);
		s.run();
		s = getScenario(2000, 200);
		s.init(25);
		s.run();
	}

	public static long[][] getResults(){
		long[][] results = new long[AGENTS.length][BLOCKSIZES.length];
		RandomGenerator rng = new MersenneTwister(15);
		
		for(int a = 0; a < AGENTS.length; a++){
			System.out.println(AGENTS[a] + " agents ");
			for(int b = 0; b < BLOCKSIZES.length; b++){
				System.out.print(" " + BLOCKSIZES[b] + ": " );
				long totalTime = 0;
					
				for(int i = 0; i < SAMPLES; i++){
					System.out.print(".");
					Scenario s = getScenario(TICKS[a], AGENTS[a]);
					s.init(BLOCKSIZES[b]);
					Result res = s.run(25000);
					if(res == null){
						totalTime = Long.MAX_VALUE;
						break;
					}
					totalTime += res.runtime;	
				}
				
				results[a][b] = totalTime == Long.MAX_VALUE? Long.MAX_VALUE : (long) (totalTime / SAMPLES);
				System.out.println( " " + (results[a][b] == Long.MAX_VALUE ? "n/a" : results[a][b]));
			}
		}
		
		return results;
	}
	
	public static String rep(String base, int times){
		String result = "";
		for(int i = 0; i < times; i++) result += base;
		return result;
	}
	
	private static Scenario getScenario(int ticks, int trucks){
		AgentsPolicy policy = new SingleThreaded();
		
		Scenario s = new BlocksizeScenario(rng.nextInt(), policy, Standards.SPEED,
				ticks, trucks, Standards.PROPORTION, Standards.FIND_PACKAGE_RADIUS);
		return s;
	}
}
