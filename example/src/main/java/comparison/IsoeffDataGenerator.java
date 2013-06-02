package comparison;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import plots.Standards;

import comparison.Scenario.Result;

public abstract class IsoeffDataGenerator {
	private static final String PATH = "/tmp/";
	
	private int[] agents;
	private int[] cutOff;
	private int[] ticks;
	private final int[] THREADS = new int[]{1, 2, 4, 8};
	private final int SAMPLES = 8;
	
	public IsoeffDataGenerator(int[] agents, int[] cutOff, int baseTicks) {
		this.agents = agents;
		this.cutOff = cutOff;
		
		if(cutOff.length != 4) throw new IllegalStateException();
		
		this.ticks = new int[agents.length];
		for(int i = 0; i < agents.length; i++){
			ticks[i] = baseTicks / agents[i];
		}
	}
	
	public void run(String fileName){
		long[][][] results = getResults();
		
		try {
			FileWriter writer = new FileWriter(new File(PATH + fileName + "_DATA"));
			for(int a = 0; a < agents.length; a++){
				for(int i = 0; i < SAMPLES; i++){
					for(int t = 0; t < THREADS.length; t++){
						writer.write(results[a][t][i] + " ");
					}
				}
				writer.write("\n");
			}
			writer.close();
			
			
			FileWriter writer2 = new FileWriter(new File(PATH + fileName + "_DATA"));
			for(int a = 0; a < agents.length; a++){
				for(int t = 0; t < THREADS.length; t++){
					double avg = 0;
					for(int i = 0; i < SAMPLES; i++){
						avg += results[a][t][i];
					}
					avg /= SAMPLES;
					writer2.write(avg + " ");
				}
				writer2.write("\n");
			}
			writer2.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public long[][][] getResults(){
		long[][][] results = new long[agents.length][THREADS.length][SAMPLES];
		
		for(int i = 0; i < SAMPLES; i++){
			System.out.println("Sample: " + i);
			
			for(int a = 0; a < agents.length; a++){
				System.out.print("Agents " + agents[a] + ": ");
				
				for(int t = 0; t < THREADS.length; t++){
					if(cutOff[t] > agents[a]) continue;
					
					Scenario s = getScenario(ticks[a], agents[a], THREADS[t]);
					
					if(ticks[a] > 1000){
						s.init(Standards.getBlocks(agents[a]));
					}
					else {
						s.init(Standards.getBlocks(agents[a]));
						s.warmupTicks(150);
					}
						
					Result res = s.run();
					results[a][t][i] = res.runtime; 
					
					System.out.print(res.runtime + "  ");
				}
				System.out.println("");
			}
		}
		return results;
	}

	protected abstract Scenario getScenario(int ticks, int agents, int threads);
}
