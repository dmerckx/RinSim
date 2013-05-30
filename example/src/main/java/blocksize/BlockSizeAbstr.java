package blocksize;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;

import plots.Standards;

import comparison.Scenario;
import comparison.Scenario.Result;

public abstract class BlockSizeAbstr {
	
	protected static final int CORES = 4;
	
	protected static final double SAMPLES = 8; //8;

	protected static int BASE_TICKS = 2500 * 100;
	protected static long MAX_TIME = 25000;
	protected static int[] AGENTS;
	protected static int[] TICKS;
	
	protected static final int AMOUNT = 10;
	
	protected static final int[] BLOCKSIZES = new int[]{0, 5, 10, 15, 20, 25, 30, 35, 45};
	//public static final String[] NAMES = new String[]{"Naive", "GradientField", "ContractNet"};
	
	protected static final RandomGenerator rng = new MersenneTwister();
	
	public BlockSizeAbstr() {
		// TODO Auto-generated constructor stub
	}
	
	public void runScenario(){
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
	
	protected void warmup(){
		Scenario s = getScenario(1000, 200);
		s.init(Standards.getBlocks(200));
		s.run();
	}

	public long[][] getResults(){
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
					Result res = s.run(MAX_TIME);
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
	
	protected abstract Scenario getScenario(int ticks, int trucks);
	
	public static String rep(String base, int times){
		String result = "";
		for(int i = 0; i < times; i++) result += base;
		return result;
	}
}
