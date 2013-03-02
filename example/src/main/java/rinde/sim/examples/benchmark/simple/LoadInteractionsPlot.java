package rinde.sim.examples.benchmark.simple;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;

import rinde.sim.core.simulation.policies.TimeUserPolicy;
import rinde.sim.core.simulation.policies.parallel.CustomPool;
import rinde.sim.core.simulation.policies.parallel.FastSingleCustomPool;
import rinde.sim.core.simulation.policies.parallel.FastSinglePool;
import rinde.sim.core.simulation.policies.parallel.PBatchTimeUserPolicy;
import rinde.sim.core.simulation.policies.parallel.SingleThreaded;

public class LoadInteractionsPlot {

	public static final int CORES = 4;
	public static final int REPS = 6;
	public static final String PATH = "/home/dmerckx/Documents/plots/";
	
	private static int TICKS = 100;
	private static int AGENTS = 1000;
	private static final int SEED = 13;
	
	private static RandomGenerator rng;

	private static int LOAD = 64;
	private static final int[] INTERACTIONS =
			new int[]{0, 2, 4, 6, 8, 10, 15, 20, 25, 30, 35, 40, 50, 60, 70, 85, 100};
	

	public static void main(String[] args) {
		rng = new MersenneTwister(SEED);
		
		TestProblem problem = new TestProblem(			
			LOAD,
			0.05,
			rng.nextLong(),
			getPolicy(0),
			500,
			200);
		problem.init();
		System.out.println("Warming up..");
		problem.run();
		problem.close();
		calc(5, REFERENCE_POLICY);
		System.out.println("Warmup completed.");
		
		
		/*AGENTS = 250;
		LOAD = 16; 
		run();
		
		AGENTS = 750;
		LOAD = 16; 
		run();
		
		TICKS = 300;
		
		AGENTS = 250;
		LOAD = 64;
		run();*/
		
		TICKS = 100;
		
		AGENTS = 750;
		LOAD = 64;
		run();
		
		TICKS = 50;
		
		AGENTS = 250;
		LOAD = 256;
		run();
		
		AGENTS = 750;
		LOAD = 256;
		run();
	}
	
	public static void run() {
		double[][] results = new double[INTERACTIONS.length][NR_POLICIES];

		for(int i = 0; i < INTERACTIONS.length; i++){
			results[i] = getResults(INTERACTIONS[i]);
		}
		 
		try {
			FileWriter writer = new FileWriter(new File(PATH + "INTERACTIONSa" + AGENTS + "l" + LOAD));
			for(int i = 0; i < INTERACTIONS.length; i++){
				for(int p = 0; p < NR_POLICIES; p++){
					writer.write(results[i][p] + " ");
				}
				writer.write("\r\n");
			}
			writer.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
	
	public static double[] getResults(int interactionsPerc){
		if(AGENTS == 750 && LOAD == 64){
			switch(interactionsPerc){
			case 0: return new double[]{
					2.014680398566421,  2.014482052493748,
					2.016427529662186, 2.0033189739573136,
					2.0133324149874054, 2.0080767041551355,
					2.032168040520409, 2.02701444336576,
					2.019611907299933
			};
			case 2:return new double[]{
					1.9706893554555462, 1.9684081569656395,
					1.96155842657746, 1.9603900470746471,
					1.9602393898057597, 1.9498251380740346,
					2.0204463543110616, 1.9006445483504404, 
					1.9207172439759035
			};
			case 4:return new double[]{
					1.9462085669603693, 1.9432437064401873,
					1.9444656161277414, 1.9266604991881586,
					1.9214728200493327, 1.8991234600067, 
					2.009165009548936, 1.76932346209862,
					1.821268583462726
			};
			case 6:return new double[]{
					1.9307200423776913, 1.932804302948808,
					1.9184525152267087, 1.8982552732413227
					, 1.8475993917010645, 1.835305542567349
					, 1.9958929828678713
					, 1.6775264645933328
					, 1.7424873651140553
			};
			case 8:return new double[]{
					1.9300581515704691
					, 1.9337701517865564
					, 1.9167834781796096
					, 1.8709750980571012
					, 1.712459717143382
					, 1.7339132345979578
					, 1.9894376442746018
					, 1.6146464725978873
					, 1.655461574470146
			};
			case 10:return new double[]{
					1.9291212617394085
					 
					, 1.9283228294610102
					 
					, 1.8811826674252574
					 
					, 1.799003089019938
					 
					, 1.4490499886903414
					 
					, 1.586711868604777
					 
					, 1.9797581797813573
					 
					, 1.5437444462852237
					 
					, 1.4997439461555342
			};
			case 15:return new double[]{
					1.8384268853049828
					 
					, 1.8447761463908579
					 
					, 1.4598978306908592
					 
					, 1.5299547117749386
					 
					, 0.8464362456754583
					 
					, 0.9634947917158534
					 
					, 1.8703778392945536
					 
					, 1.2781055822810183
					 
					, 0.9202415528656216
			};
			case 20:return new double[]{
					1.1919299843527407
					 
					, 1.2031128450531576
					 
					, 0.8959876759945228
					 
					, 1.0452241495832395
					 
					, 0.7221809511680888
					 
					, 0.7350363649456326
					 
					, 1.1289553724492618
					 
					, 0.9055821215965648
					 
					, 0.7289838098026753
			};
			case 25:return new double[]{
					 0.878675174353109
					 
					 , 0.8690495428449926
					  
					 , 0.7928988802408685
					  
					 , 0.8157898938930627
					  
					 , 0.6598248706871501
					  
					 , 0.6656970115511659
					  
					 , 0.8802041607442822
					  
					 , 0.7920691152075162
					  
					 , 0.664869665933551
			};
			case 30:return new double[]{
					0.8082104064886402
					 
					, 0.8090930674264007
					 
					, 0.7387813796193257
					 
					, 0.743953021306322
					 
					, 0.6280222615361934
					 
					, 0.623974659440457
					 
					, 0.8086195169020992
					 
					, 0.7413635572572224
					 
					, 0.6200992284924062
			};
			case 35:return new double[]{
					0.7646924539317097
					 
					, 0.7629507658071406
					 
					, 0.6940349709254932
					 
					, 0.69527241299525
					 
					, 0.583144502599871
					 
					, 0.5846929957593412
					 
					, 0.7642291322267685
					 
					, 0.694218891206708
					 
					, 0.598941845468402
					 
			};
			default:
			}
		}
		
		double interactions = (1.0d * interactionsPerc) / 100;
		
		double[] results = new double[NR_POLICIES];
		
		System.out.println("-----Interactions: " + interactions + " -----");
		
		double reference = calc(interactions, REFERENCE_POLICY);

		System.out.println("Reference runtime: " + reference);
		System.out.println(" ");
		
		for(int p = 0; p < NR_POLICIES; p++){
			results[p] = reference / calc(interactions, p);
			System.out.println(p + "runtime perc" + results[p]);
			System.out.println(" ");
		}
		
		System.out.println("----------");
		System.out.println(" ");
		
		return results;
	}
	
	public static double calc(double interactions, int policyNr){
		double result = 0;
		
		if(getPolicy(policyNr) == null) return 0;
		
		int reps = policyNr == REFERENCE_POLICY ? REPS * 2 : REPS;
		for(int r = 0; r < reps; r++){
			TestProblem problem = new TestProblem(			
				LOAD,
				interactions,
				rng.nextLong(),
				getPolicy(policyNr),
				TICKS,
				AGENTS);
			problem.init();
			
			long before = System.currentTimeMillis();
			problem.run();
			result += System.currentTimeMillis() - before;
			
			problem.close();
			//System.out.println("done");
		}
		
		return result / reps;
	}
	
	private static final int NR_POLICIES = 9;
	private static final int REFERENCE_POLICY = 100;
	public static TimeUserPolicy getPolicy(int nr){
		switch(nr){
			case 0: return new FastSinglePool(CORES);
			case 1: return new FastSinglePool(CORES*2);
			//Batch
			case 2: return new PBatchTimeUserPolicy(2, CORES);
			case 3: return new PBatchTimeUserPolicy(2, CORES*2);
			//Batch
			case 4: return new PBatchTimeUserPolicy(5, CORES);
			case 5: return new PBatchTimeUserPolicy(5, CORES*2);
			//Batch
			case 6: return new FastSingleCustomPool(CORES);
			case 7: return new CustomPool(2, CORES);
			case 8: return new CustomPool(5, CORES);
			//
			case 100: return new SingleThreaded();
			default:
				throw new IllegalArgumentException("Unknown policy nr");
		}
	}
}

