package rinde.sim.examples.benchmark;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import rinde.sim.core.simulation.policies.ParallelExecution;

public class PdpBenchmark {

	public static final int CORES = 4;
	public static final int CARS = 10;
	public static final int PARCELS = 40;
	
	public static final int REPS = 9;
	public static final String PATH = "/home/dmerckx/Documents/";
	
	public static void main(String[] args) {
		
		double from = 1;
		double stepsize = 1;
		double to = 10;
		
		int nrSteps = (int) Math.floor((to - from) / stepsize);
		
		double[] results = new double[nrSteps];
		
		//Dummy problem to startup threads
		new PdpProblem(100, 200, 3).run();
		
		for(int i = 0; i < nrSteps; i++){
			double multiplier = from + i * stepsize;
			
			for(int j = 0; j < REPS; j++){
				PdpProblem problem = new PdpProblem(			
					(int) (CARS * multiplier),
					(int) (PARCELS * multiplier),
					(long) (Math.random()*10000));
				long before = System.currentTimeMillis();
				problem.run();
				results[i] += System.currentTimeMillis() - before;
			}
			results[i] /= REPS;
			System.out.println(i + " runtime: " + results[i]);
		}
		
		ParallelExecution.close();
		
		
		try {
			FileWriter writer = new FileWriter(new File(PATH + "PDP" + CORES));
			
			for(int i = 0; i < nrSteps; i++){
				writer.write(results[i] + " ");
			}
			writer.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
}
