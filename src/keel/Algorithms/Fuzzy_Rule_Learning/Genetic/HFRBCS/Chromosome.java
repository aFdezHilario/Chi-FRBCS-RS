package keel.Algorithms.Fuzzy_Rule_Learning.Genetic.HFRBCS;

import java.util.ArrayList;

import org.core.Randomize;

public class Chromosome implements Comparable{
	
	boolean [] individual;
	double fitness;
	boolean evaluate;
	
	/**
	 * Default constructor
	 */
	public Chromosome(){
		evaluate = false;
		individual = new boolean[1];
		for (int j = 0; j < individual.length; j++){
			individual[j] = true; 
		}
		fitness = 0;
	}
	
	/**
	 * Constructor with parameters
	 * @param size length of the chromosome 
	 * @param value to be assigned to all genes
	 */
	public Chromosome(int size, boolean value){
		evaluate = false;
		individual = new boolean[size];
		for (int j = 0; j < individual.length; j++){
			individual[j] = value; 
		}
		fitness = 0;
	}
	
	/**
	 * Constructor with parameters
	 * @param size length of the chromosome
	 */
	public Chromosome(int size){
		evaluate = false;
		individual = new boolean[size];
		for (int j = 0; j < individual.length; j++){
			individual[j] = Randomize.Rand() > 0.5; 
		}
		fitness = 0;
	}
	
	/**
	 * Constructor with parameters
	 * @param size length of the chromosome
	 * @param pos variable id to set 1 
	 */
	public Chromosome(int size, int pos){
		evaluate = false;
		individual = new boolean[size];
		for (int j = 0; j < individual.length; j++){
			individual[j] = false; 
		}
		individual[pos] = true;;
		fitness = 0;
	}
	
	/**
	 * Check if the chromosome has been evaluated
	 * @return true if the chromosome has been evaluated; false otherwise
	 */
	public boolean isEvaluated(){
		return evaluate;
	}
	
	/**
	 * Set the chromosome as evaluated
	 */
	public void evaluated(){
		evaluate = true;
	}
	
	/**
	 * Set the fitness value for the chromosome
	 * @param fitness value of the fitness
	 */
	public void setFitness(double fitness){
		this.fitness = fitness;
	}
	
	/**
	 * It gets the fitness of the chromosome
	 * @return the fitness value
	 */
	public double getFitness(){
		return fitness;
	}
	
	/**
	 * It computes the hamming distance between chromosomes
	 * @param c the other chromosome
	 * @return the number of different genes between chromosomes
	 */
	public int hamming(Chromosome c){
		int dist = 0;
		for (int i=0; i<individual.length; i++) if (c.individual[i] != individual[i]) dist++;
				
		return dist;
	}
	
		
	/**
	 * It creates a complete copy of the chromosome
	 */
	public Chromosome clone(){
		Chromosome c = new Chromosome();
		c.individual = individual.clone();
		c.fitness = this.fitness;
		c.evaluate = false;
		return c;
	}
	
	/**
	 * It gets the whole chromosome representation
	 * @return the genotype of the chromosome
	 */
	public boolean [] getChromosome(){
		return this.individual;
	}
	
	/**
	 * Obtains an array with the positions (indices) of the genes that are different between chromosomes
	 * @param c the other chromosome
	 * @return an ArrayList with the indices
	 */
	public ArrayList <Integer> differ (Chromosome c){
		ArrayList <Integer> differ = new ArrayList <Integer>();
		boolean [] cChrom = c.getChromosome();
		for (int i = 0; i < individual.length; i++){
			if (individual[i] != cChrom[i]){
				differ.add(i);
			}
		}
		return differ;
	}
	
	/**
	 * It changes the value of the genes stated in positions
	 * @param positions
	 */
	public void flip(int [] positions){
		for (int i = 0; i < positions.length; i++){
			individual[positions[i]] = !individual[positions[i]];
		}
	}
		
	/**
	 * Compares the fitness of two Chrmosomes for the ordering procedure
	 * @param a Object a Chromosome
	 * @return int -1 if the current Chrosome is worst than the one that is compared, 1 for the contrary case and 0
	 * if they are equal.
	 */
	public int compareTo(Object a) {
		if ( ( (Chromosome) a).fitness < this.fitness) {
			return -1;
		}
		if ( ( (Chromosome) a).fitness > this.fitness) {
			return 1;
		}
		return 0;
	}


}
