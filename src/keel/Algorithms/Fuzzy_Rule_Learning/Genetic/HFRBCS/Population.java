package keel.Algorithms.Fuzzy_Rule_Learning.Genetic.HFRBCS;

import org.core.*;
import java.util.ArrayList;
import java.util.Collections;

public class Population {
	
	ArrayList <Chromosome> population;
	KnowledgeBase kb;
	int nEvals, popSize;
	double threshold;
	double bestFitness;
	boolean newIndividuals;
	myDataset train;
	
	/**
	 * Default constructor
	 */
	public Population(){
	}
	
	/**
	 * Constructor with parameters
	 * @param kb Rule base
	 * @param popSize size of the population
	 * @param nEvals number of evaluations
	 * @param bitsGen bits per gen (for gray codification: incest prevention)
	 */
	public Population(KnowledgeBase kb, myDataset train, int nEvals){
		this.kb = kb;
		this.nEvals = nEvals;
		this.popSize = kb.size();
		population = new ArrayList<Chromosome>();
		this.train = train;
		threshold = popSize/4.0; //number of rules / 4
		bestFitness = 0;
	}
	
	/**
	   * Maximization
	   * @param a double first number
	   * @param b double second number
	   * @return boolean true if a is better than b
	   */
	  public boolean BETTER(double a, double b) {
	    if (a > b) {
	      return true;
	    }
	    return false;
	  }
	
	/**
	 * It sets the initial population.
	 * 
	 * First chromosome has all bits active (all rules selected). The remaining ones are set up randomly
	 */
	private void Initialize(Chromosome ini){
		System.out.println("Initilization");
		population.clear();
		population.add(ini);
		for (int i = 1; i < popSize; i++){
			Chromosome c = new Chromosome(kb.size());
			population.add(c);
		}
	}
	
	/**
	 * It evaluates those chromosomes which have not been evaluated yet
	 */
	private void Evaluate(){
		//System.out.println("Evaluation...");
		for (int i = 0; i < population.size(); i++){
			Chromosome c = population.get(i);
			if (!c.evaluate){
				newIndividuals = true; //Al menos hay un cromosoma nuevo en la poblacion
				kb.updateSelected(c.getChromosome());
				double acc = classify();
				c.setFitness(acc);
				c.evaluated();
				nEvals--;
			}
		}
	}
	
	/**
	 * Crossover function (one point crossover)
	 */
	public void Cross(){
		//System.out.println("Cross");
		
		//Order chromosomes at random
		int [] sample = new int[population.size()];

		for (int i=0; i<sample.length; i++) sample[i] = i;

		for (int i=0; i<sample.length; i++){
			int j = Randomize.RandintOpen(0, sample.length);
			int temp = sample[j];
			sample[j] = sample[i];
			sample[i] = temp;
		}
		//Select two parents
		for (int i=0; i<sample.length-2; i+=2){
			Chromosome mom = population.get(sample[i]);
			Chromosome dad = population.get(sample[i+1]);
			//Compute hamming distance
			double dist = mom.hamming(dad)/2.0;
			if (dist > threshold){ 
				HUX(mom,dad);
				//OnePoint(mom,dad);
				//xPC_BLX(mom,dad);
			}
		}
	}
	
	private void HUX(Chromosome mom, Chromosome dad){
		Chromosome son1 = mom.clone();
		Chromosome son2 = dad.clone();
		ArrayList <Integer> positions = son1.differ(son2);

		int exchanges = positions.size() / 2;
		if ((positions.size() >0) && (exchanges == 0)) 
			exchanges = 1;

		int [] flips = new int[exchanges];
		for (int j = 0; j < exchanges; j++) {
			int index = Randomize.RandintClosed(0, positions.size()-1);
			flips[j] = positions.get(index);
			positions.remove(index);
		}
		son1.flip(flips);
		son2.flip(flips);

		//Insert
		population.add(son1);
		population.add(son2);
	}


	public double Select(){
		//System.out.println("Select");
		Collections.sort(population);
		double bestFitness = population.get(0).getFitness();
		for (int i = population.size()-1; i > popSize; i--){
			population.remove(i);
		}
		return bestFitness;
	}

	public void Restart(){
		Chromosome best = population.get(0);
		population.removeAll(population);
		Initialize(best);
	}

	/**
	 * It launches the evolutionary process
	 */
	public void Generation(){
		int resets = 0;
		boolean output = false;
		Chromosome c = new Chromosome(kb.size(),true);
		Initialize(c);
		Evaluate();
		System.out.println("Initilization complete...");
		do{
			newIndividuals = false;
			Cross();
			Evaluate();
			double bestFitness = Select();
			if (bestFitness > this.bestFitness){
				this.bestFitness = bestFitness;
				resets = 0;
				output = true;
			}
			if (!newIndividuals){ //No new chromosomes in the population
				threshold--; //reduce threshold for incest prevention
				if (threshold < 0){
					System.out.println("*** Restarting ***");
					Restart();
					Evaluate();
					threshold = kb.size()/4.0;
		            resets++;              
				}
			} 
			if (output){
				System.out.println("Evaluations remaining: "+nEvals+", Best Accuracy: "+bestFitness+".");
				output = false;
			}
		}while((nEvals > 0)&&(bestFitness < 1.0)&&(resets < 3));
	}
	
	/**
	 * Obtains the best RB
	 */
	public void updateBest(){
		Collections.sort(population);
		kb.updateSelected(population.get(0).getChromosome());
	}
	
	/**
	 * It generates the output file from a given dataset and stores it in a file
	 * @param dataset myDataset input dataset
	 * @param filename String the name of the file
	 *
	 * @return The classification accuracy
	 */
	private double classify() {
		int hits, vars;
		hits = 0;
		vars = train.getNumberVariables();
		String [] input = new String[vars];
		for (int i = 0; i < train.size(); i++){
			byte classIndex = 0;
			input = train.getExample(i);
			classIndex =  train.getClass(i);
			byte classOut = kb.classify(kb.FRM_WINNING_RULE, input); //change for Additive Combination
			hits += classIndex == classOut ? 1 : 0;
		}
		return 1.0*hits/train.size();
	}

}
