package batchsize.adaptive;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;

import plots.Standards;
import rinde.sim.core.simulation.policies.AgentsPolicy;
import rinde.sim.core.simulation.policies.Policies;

import comparison.Scenario;
import comparison.Scenario.Result;

public abstract class BSAdaptiveAbstr {
	public static final String PATH = "/tmp/";
	
	protected static int SAMPLES = 8;
	protected static int CORES = 4;
	
	private int baseTicks;

	public final int[] agents;
	public final int[] ticks;
	//public static final String[] NAMES = new String[]{"Naive", "GradientField", "ContractNet"};
	
	protected static final RandomGenerator rng = new MersenneTwister();
	
	public BSAdaptiveAbstr(int[] agents, int baseTicks) {
		this.agents = agents;
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
		long[] results = getResults();
		
		System.out.println("    ");
		System.out.println("    ");
		
		String str = 
		"\\begin{tabular}{|l || r | r|}\n"+
		"\\hline \n"+
		"agents & minimum & adaptive \\\\\n"+ 
		"\\hline\n";

		for(int a = 0; a < agents.length; a++){
			str += agents[a] + "& TODO & " + results[a] + "\\\\\n";
		}
		
		str +=
		"\\hline\n"+
		"\\end{tabular}\n\n";
		
		System.out.println(str);
		
		return str;
	}
	
	private void warmup(){
		int agents = 200;
		Scenario s = getScenario(2000, agents, Policies.getAdaptive(CORES));
		s.init(Standards.getBlocks(agents));
		s.run();
	}

	public long[] getResults(){
		long[] results = new long[agents.length];
		

		for(int i = 0; i < SAMPLES; i++){
			System.out.println("Sample: " + i);
			
			for(int a = 0; a < agents.length; a++){
				System.out.print("Agents " + agents[a] + ": ");
					
				Scenario s = getScenario(ticks[a], agents[a], Policies.getAdaptive(CORES));
					
				if(ticks[a] > 1500){
					s.init(Standards.getBlocks(agents[a]));
				}
				else {
					s.init(Standards.getBlocks(agents[a]));
					s.warmupTicks(150);
				}
						
				Result res = s.run();
				
				results[a] += res.runtime;
					
				System.out.println(res.runtime + "  ");
			}
		}
		
		for(int a = 0; a < agents.length; a++){
			results[a] = results[a] / SAMPLES;
		}
		
		return results;
	}
	
	public static String rep(String base, int times){
		String result = "";
		for(int i = 0; i < times; i++) result += base;
		return result;
	}
	
	protected abstract Scenario getScenario(int ticks, int trucks, AgentsPolicy policy);
}
