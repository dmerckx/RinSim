package batchsize;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;

import plots.Standards;

import comparison.Scenario;
import comparison.Scenario.Result;

public abstract class BatchSizeTableAbstr {
	public static final String PATH = "/tmp/";
	
	protected static int SAMPLES = 11;
	protected static int CORES = 4;
	
	private int baseTicks;

	public final int[] agents;
	public final int[] batchsizes;
	public final int[] ticks;
	//public static final String[] NAMES = new String[]{"Naive", "GradientField", "ContractNet"};
	
	protected static final RandomGenerator rng = new MersenneTwister();
	
	public BatchSizeTableAbstr(int[] agents, int[] batchsizes, int baseTicks) {
		this.agents = agents;
		this.batchsizes = batchsizes;
		this.baseTicks = baseTicks;

		
		this.ticks = new int[agents.length];
		for(int i = 0; i < agents.length; i++){
			ticks[i] = baseTicks / agents[i];
		}
	}
	
	public String run(){
		warmup();
		return runScenario();
	}
	
	public String runScenario(){
		long[][] results = getResults();
		
		System.out.println("    ");
		System.out.println("    ");
		
		String str = 
		"\\begin{tabular}{l l l " + rep("r|", batchsizes.length) + "}\n"+
		"\\cline{5-"+(batchsizes.length+3)+"}\n"+
		"& & & & \\multicolumn{"+(batchsizes.length-1)+"}{c|}{batchsize} \\\\\n"+
		"\\hline \n"+
		"\\multicolumn{1}{|c|}{agents} & \\multicolumn{1}{c|}{ticks}  & \\multicolumn{1}{c||}{$f_{q}$} & \\multicolumn{1}{c|}{ref}" + getBlocksizesHeader() +" \\\\\n"+ 
		"\\hline\n";

		for(int a = 0; a < agents.length; a++){
			str += "\\multicolumn{1}{|c|}{"+agents[a]+"} & \\multicolumn{1}{c|}{"+ticks[a]+"}";
			str += "& \\multicolumn{1}{c||}{"+results[a][0]+"}";
			for(int b = 0; b < batchsizes.length; b++){
				
				for(int b2 = 1; b2 < batchsizes.length; b2++){
					if(results[a][b+1] > results[a][b2+1]){
						str += " & " + (results[a][b+1] == Long.MAX_VALUE? " ":results[a][b+1]) + " ";
						break;
					}
					if(b2 == (batchsizes.length - 1)){
						str += " & \\textbf{" + results[a][b+1] + "} ";
					}
				}
				
			}
			str +=  "\\\\\n";
		}
		
		str +=
		"\\hline\n"+
		"\\end{tabular}\n\n";
		
		System.out.println(str);
		
		return str;
	}
	
	private String getBlocksizesHeader() {
		String result = "";
		for(int b = 1; b < batchsizes.length; b++){
			result += " & " + batchsizes[b];
		}
		return result;
	}
	
	private void warmup(){
		int agents = 200;
		Scenario s = getScenario(2000, agents, 5);
		s.init(Standards.getBlocks(agents));
		s.run();
	}

	public long[][] getResults(){
		long[][] results = new long[agents.length][batchsizes.length+1];
		

		for(int i = 0; i < SAMPLES; i++){
			System.out.println("Sample: " + i);
			
			for(int a = 0; a < agents.length; a++){
				System.out.print("Agents " + agents[a] + ": ");
				
				for(int b = 0; b < batchsizes.length; b++){
					
					if(batchsizes[b] > agents[a]){
						results[a][b+1] = Long.MAX_VALUE;
						continue;
					}
					if(results[a][b+1] == Long.MAX_VALUE) continue;
					
					Scenario s = getScenario(ticks[a], agents[a], batchsizes[b]);
					
					if(ticks[a] > 1500){
						s.init(Standards.getBlocks(agents[a]));
					}
					else {
						s.init(Standards.getBlocks(agents[a]));
						s.warmupTicks(150);
					}
						
					Result res = s.run();
					if(res == null){
						results[a][b+1] = Long.MAX_VALUE;
						break;
					}
					
					System.out.print(res.runtime + "  ");
					
					if(b==0) results[a][0] += res.queriesPerformed;
					results[a][b+1] += res.runtime;
				}
				
				System.out.println("");
			}
		}
		
		for(int a = 0; a < agents.length; a++){
			results[a][0] = (agents[a] * ticks[a]) / (results[a][0] / (SAMPLES));
			for(int b = 0; b < batchsizes.length; b++){
				if(results[a][b+1] == Long.MAX_VALUE) continue;
				
				results[a][b+1] = results[a][b+1] / SAMPLES;
			}
		}
		
		return results;
	}
	
	public static String rep(String base, int times){
		String result = "";
		for(int i = 0; i < times; i++) result += base;
		return result;
	}
	
	protected abstract Scenario getScenario(int ticks, int trucks, int batchSize);
}
