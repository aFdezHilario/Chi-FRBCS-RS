/*
 * Copyright (C) 2014 Mikel Elkano Ilintxeta
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package keel.Algorithms.Fuzzy_Rule_Learning.Genetic.HFRBCS;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.core.Files;

import java.util.Map.Entry;
import java.io.Serializable;

/**
 * Knowledge Base
 * @author Mikel Elkano Ilintxeta
 * @version 1.0
 * @author Alberto Fernandez
 * @version 1.1
 * @date 17/05/2017
 */
public class KnowledgeBase implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -8439845829588093846L;
	public static final byte FRM_WINNING_RULE = 0;
	public static final byte FRM_ADDITIVE_COMBINATION = 1;

	/**
	 * Rule base
	 */
	private ArrayList<FuzzyRule> ruleBaseOk;
	private DataBase dataBase;

	/**
	 * Rule Base
	 */
	private byte[][] ruleBase; // Antecedents of each rule
	private ArrayList<Byte>[] rulesClasses; // Classes of each rule
	private float[][] matchingDegrees; // Matching degrees of the classes of each rule
	private boolean[] ruleFired; // To check for fired rules

	/**
	 * Dataset
	 */
	//private String[][] inputValues; // Input values of the instances of the current split of the dataset
	//private byte[] classLabels; // Indices of the instances class labels

	/**
	 * Temporary structures
	 */
	private HashMap<ByteArrayWritable,ArrayList<Byte>> ruleBaseTmp; // Key: antecedents of the rule, Value: Classes of the rule
	private float[][] membershipDegrees; // Pre-computed membership degrees of a given example
	private String[] input;
	private byte classIndex;
	private ByteArrayWritable newRule;
	private ArrayList<Byte> classEntry;

	private long repRulesNoClass;
	private boolean [] uniqueM, uniqueRIndex;
	private boolean [] selected;

	private String fileRB;

	/**
	 * Default constructor
	 */
	public KnowledgeBase (){

		ruleBaseOk = new ArrayList<FuzzyRule>();
	}

	/**
	 * Constructor
	 * @param db dataBase
	 * @param fileRB file where the RB must be stored
	 */
	public KnowledgeBase (DataBase db, String fileRB){

		ruleBaseOk = new ArrayList<FuzzyRule>();
		dataBase = db;
		this.fileRB = fileRB;

	}	

	/**
	 * Adds a DataBase to the KB
	 * @param db the database
	 */
	public void setDataBase(DataBase db){
		this.dataBase = db;
	}

	/**
	 * Creates a new fuzzy rule from an instance
	 * @param instanceStr string of the instance used to create a single rule
	 */
	public void addFuzzyRule (FuzzyRule newFuzzyRule){
		ruleBaseOk.add(newFuzzyRule);
	}

	/**
	 * Obtains the index of the maximum value of the array
	 * @param degrees array (contains association degrees for the classes)
	 * @return the class index corresponding to the highest association degree
	 */
	byte maxIndex(double [] degrees){
		byte classIndex = 0;
		double max = degrees[0];
		for (byte i = 1; i < degrees.length; i++){
			if (degrees[i] > max){
				max = degrees[i];
				classIndex = i;
			}
		}
		return classIndex;
	}

	/**
	 * Classifies an example
	 * @param frm fuzzy reasoning method to be used (0: winning rule, 1: additive combination)
	 * @param example input example
	 * @return predicted class
	 */
	public byte classify (byte frm, String[] example){
		if (frm == FRM_WINNING_RULE)
			return (byte)FRM_WR(example)[0]; 
		else
			return (byte)FRM_AC(example)[0];
	}

	/**
	 * Classifies an example
	 * @param frm fuzzy reasoning method to be used (0: winning rule, 1: additive combination)
	 * @param example input example
	 * @return predicted class
	 */
	public byte classifyOK (byte frm, double[] example){
		if (frm == FRM_WINNING_RULE)
			return FRM_WR_Class(example); 
		else
			return FRM_AC_Class(example);
	}

	/**
	 * Classifies an example
	 * @param frm fuzzy reasoning method to be used (0: winning rule, 1: additive combination)
	 * @param example input example
	 * @return predicted class
	 */
	public double [] classifyDegrees (byte frm, String[] example){
		if (frm == FRM_WINNING_RULE)
			return FRM_WR(example);
		else
			return FRM_AC(example);
	}

	/**
	 * Additive Combination Fuzzy Reasoning Method
	 * @param example input example
	 * @return a double array where [0] is the predicted class index and [1] is the confidence degree
	 */
	private double[] FRM_AC (String[] example){

		double[] output = new double[dataBase.getNumClasses()+1];
		output[0] = dataBase.getMostFrequentClass(); // Default class
		for (int i = 1; i < output.length; i++)
			output[i] = 0.0; // Default confidence

		double[] classDegree = new double[dataBase.getNumClasses()];
		for (byte i = 0; i < classDegree.length; i++) classDegree[i] = 0.0;

		double degree;

		// Compute the confidence of each class
		//for (FuzzyRule rule:ruleBaseOk) {
		for (int i = 0; i <  ruleBaseOk.size(); i++) {
			if(selected[i]){
				FuzzyRule rule = ruleBaseOk.get(i);	
				degree = this.computeAssociationDegree(example, rule);
				classDegree[rule.getClassIndex()] += degree;
			}
		}
		for (int i = 1; i < output.length; i++) //Normalize??
			output[i] = classDegree[i-1]; 

		// Get the class with the highest confidence
		for (byte i = 0; i < classDegree.length; i++) {
			if (classDegree[i] > output[1]) {
				output[0] = i;
				output[1] = classDegree[i];
			}
		}
		output[0] = maxIndex(classDegree);

		return output;

	}

	/**
	 * Winning Rule Fuzzy Reasoning Method
	 * @param example input example
	 * @return a double array where [0] is the predicted class index and [1] is the confidence degree
	 */
	private double[] FRM_WR (String[] example){

		double[] output = new double[dataBase.getNumClasses()+1];
		int[] indexR = new int[dataBase.getNumClasses()];
		output[0] = dataBase.getMostFrequentClass(); // Default class
		for (int i = 1; i < output.length; i++)
			output[i] = 0.0; // Default confidence

		double degree;
		int index = 0;

		// Get the class with the rule with highest association degree
		//for (FuzzyRule rule:ruleBaseOk) {
		for (int i = 0; i <  ruleBaseOk.size(); i++) {
			if(selected[i]){
				FuzzyRule rule = ruleBaseOk.get(i);
				degree = this.computeAssociationDegree(example,rule);
				if (output[rule.getClassIndex()+1] < degree){
					output[rule.getClassIndex()+1] = degree;
					indexR[rule.getClassIndex()] = index;
				}
				index++;
			}
		}
		//Truncation

		int indexMax = 1;
		double max = output[1];
		for (int i = 2; i < output.length;i++){
			if (output[i] > max){
				max = output[i];
				output[indexMax] = 0;
				indexMax = i;
			}else{
				output[i] = 0;
			}
		}
		//indexMax-1 ; //
		ruleFired[indexR[indexMax-1]] = true;
		output[0] = indexMax-1; //maxIndex(output)-1; //output has one more element than number of classes

		return output;	
	}

	/**
	 * Winning Rule Fuzzy Reasoning Method
	 * @param example input example
	 * @return a double array where [0] is the predicted class index and [1] is the confidence degree
	 */
	private byte FRM_WR_Class (double [] example){

		byte output = dataBase.getMostFrequentClass(); // Default class
		double degree, max_degree;
		degree = max_degree = 0;

		// Get the class with the rule with highest association degree
		//for (FuzzyRule rule:ruleBase) {
		for (int i = 0; i <  ruleBaseOk.size(); i++) {
			degree = this.computeAssociationDegreeD(example,ruleBaseOk.get(i));
			if (max_degree < degree){
				max_degree = degree;
				output = ruleBaseOk.get(i).getClassIndex(); 
			}
		}
		return output;

	}

	/**
	 * Winning Rule Fuzzy Reasoning Method
	 * @param example input example
	 * @return a double array where [0] is the predicted class index and [1] is the confidence degree
	 */
	private byte FRM_AC_Class (double [] example){
		return dataBase.getMostFrequentClass(); // Default class
	}


	/**
	 * Returns the association degree of the input example with this rule
	 * @param example input example
	 * @return association degree of the input example with this rule
	 */
	public float computeAssociationDegree (String[] example, FuzzyRule r){
		return computeMatchingDegree(example,r)*r.getRuleWeight();
	}

	public float computeMatchingDegree (String[] example, FuzzyRule r){

		float matching = 1.0f;
		for (int i = 0; (i < example.length)&&(matching > 0); i++){
			// If it is a nominal value and it is not equal to the antecedent, then there is no matching
			if (dataBase.get(i) instanceof NominalVariable){
				if (!((NominalVariable)dataBase.get(i)).getNominalValue(r.getAntecedent(i)).contentEquals(example[i]))
					return 0.0f;
			}
			else
				matching *=  ((FuzzyVariable)dataBase.get(i)).getMembershipDegree(r.getAntecedent(i), Double.parseDouble(example[i]));
		}
		return matching;

	}

	/**
	 * Returns the association degree of the input example with this rule
	 * @param example input example
	 * @return association degree of the input example with this rule
	 */
	public float computeAssociationDegreeD (double[] example, FuzzyRule r){
		return computeMatchingDegreeD(example,r)*r.getRuleWeight();
	}

	public float computeMatchingDegreeD (double[] example, FuzzyRule r){

		float matching = 1.0f;
		for (int i = 0; (i < example.length)&&(matching > 0); i++){
			// If it is a nominal value and it is not equal to the antecedent, then there is no matching
			if (dataBase.get(i) instanceof NominalVariable){
				int valor = (int)example[i];
				if (!((NominalVariable)dataBase.get(i)).getNominalValue(r.getAntecedent(i)).contentEquals(String.valueOf(valor))){
					return 0.0f;
				}
			}
			else
				matching *=  ((FuzzyVariable)dataBase.get(i)).getMembershipDegree(r.getAntecedent(i), example[i]);
		}
		return matching;

	}


	/**
	 * Returns the rule base of this classifier
	 * @return rule base of this classifier
	 */
	public ArrayList<FuzzyRule> getRuleBase (){
		return ruleBaseOk;
	}

	public int size(){
		return ruleBaseOk.size();
	}

	public FuzzyRule getRule(int i){
		return ruleBaseOk.get(i);
	}

	public DataBase getDataBase(){
		return this.dataBase;
	}

	public int getNumVariables(){
		return this.dataBase.getNumVariables();
	}

	public String toString(){
		String output = new String("");
		output += "DATABASE\n;";
		output += this.dataBase.toString()+"\n";
		for (int id = 0, cont = 0; id < ruleBaseOk.size(); id++){
			if (selected[id]){
				output+= "Rule ("+cont+"): ";
				//Write antecedents
				output += ruleBaseOk.get(id).toString(dataBase);
				output += "\n";
				cont++;
			}
		}
		return output;
	}

	public KnowledgeBase clone(){
		KnowledgeBase kb = new KnowledgeBase(this.dataBase,this.fileRB);
		for (FuzzyRule fr:ruleBaseOk){
			kb.addFuzzyRule(fr.clone());
		}
		return kb;
	}

	private void computeMatchingDegreesRule(){
		matchingDegrees = new float[ruleBaseTmp.size()][dataBase.getNumClasses()];
		for (int i = 0; i < ruleBaseTmp.size(); i++)
			for (int j = 0; j < dataBase.getNumClasses(); j++)
				matchingDegrees[i][j] = 0.0f;
		membershipDegrees = new float[dataBase.getNumVariables()][dataBase.getNumLinguisticLabels()];
		rulesClasses = new ArrayList[ruleBaseTmp.size()];
		ruleBase = new byte[ruleBaseTmp.size()][dataBase.getNumVariables()];
		uniqueM = new boolean[ruleBaseTmp.size()];
		Iterator<Entry<ByteArrayWritable,ArrayList<Byte>>> iterator = ruleBaseTmp.entrySet().iterator();
		Entry<ByteArrayWritable,ArrayList<Byte>> ruleEntry;
		int i = 0;
		while (iterator.hasNext()){
			ruleEntry = iterator.next();
			ruleBase[i] = ruleEntry.getKey().getBytes(); // Antecedents of the rule
			rulesClasses[i] = ruleEntry.getValue(); // Classes of the rule (aqui hay -1!)
			if (ruleEntry.getValue().size() == 1){ 
				uniqueM[i] = true;
			}
			i++;
		}
	}

	private void computeMatchingDegreesAll(myDataset train){
		byte label;
		for (int i = 0; i < train.size(); i++){

			input = new String[dataBase.getNumVariables()];
			classIndex = 0;

			input = train.getExample(i);
			classIndex =  train.getClass(i);
			// Compute the membership degree of the current value to all linguistic labels
			for (int j = 0; j < dataBase.getNumVariables(); j++) {
				if (dataBase.get(j) instanceof FuzzyVariable)
					for (label = 0; label < dataBase.getNumLinguisticLabels(); label++)
						membershipDegrees[j][label] = dataBase.computeMembershipDegree(j,label,input[j]);
			}
			// Compute the matching degree of the example with all rules
			for (int j = 0; j < ruleBase.length; j++){
				matchingDegrees[j][classIndex] += dataBase.computeMatchingDegree(
						membershipDegrees, ruleBase[j], input);
			}
		}

	}

	private float computeRuleWeight(int i){
		float currentRW, ruleWeight, sum, sumOthers;
		ruleWeight = 0.0f;
		classIndex = -1;
		sum = 0.0f;
		for (int j = 0; j < matchingDegrees[i].length; j++){
			sum += matchingDegrees[i][j];
		}
		for (int j = 0; j < matchingDegrees[i].length; j++){
			if (rulesClasses[i].contains((byte)j)){
				sumOthers = sum-matchingDegrees[i][j];
				currentRW = (matchingDegrees[i][j] - sumOthers) / sum; //P-CF
				if (currentRW > ruleWeight){
					ruleWeight = currentRW;
					classIndex = (byte)j;
				}
			}
		}
		return ruleWeight;
	}

	public String generation(myDataset train){
		ruleBaseTmp = new HashMap<ByteArrayWritable,ArrayList<Byte>>();
		repRulesNoClass = 0;

		System.err.println("Rule Generation");
		for (int i = 0; i < train.size(); i++){

			input = new String[dataBase.getNumVariables()];
			classIndex = 0;

			input = train.getExample(i);
			classIndex =  train.getClass(i);			

			// Generate a new fuzzy rule
			byte[] antecedents = dataBase.getRuleFromExample(input);

			newRule = new ByteArrayWritable(antecedents);
			if (ruleBaseTmp.containsKey(newRule)){
				classEntry = ruleBaseTmp.get(newRule);
				if (!classEntry.contains(classIndex)){
					classEntry.add(classIndex);
					repRulesNoClass++;
				}else if (!classEntry.contains((byte)100)){ //repeated rule
					byte valor = 100;
					classEntry.add(valor);
				}
			}else{
				classEntry = new ArrayList<Byte>(); //Unique rule (although it can be generated later on)
				classEntry.add(classIndex);
				ruleBaseTmp.put(newRule, classEntry);
			}
		}

		System.err.println("Computing Matching Degrees Rule");
		/**
		 *  Transform the rule base into arrays
		 */
		computeMatchingDegreesRule();

		System.err.println("Computing Matching Degrees All");
		/**
		 *  Compute the matching degree of all the examples with all the rules
		 */
		computeMatchingDegreesAll(train);

		//System.err.println("Rule Base: "+ruleBase.length);

		System.err.println("Computing Rule Weights");

		String rbString = new String("");

		int [] classes = new int[dataBase.getNumClasses()];
		uniqueRIndex = new boolean[ruleBase.length]; //check fired rules
		int uniqueR, dobleCqt;
		uniqueR = dobleCqt = 0;

		/**
		 * Compute the rule weight of each rule and solve the conflicts
		 */
		for (int i = 0; i < ruleBase.length; i++){
			float ruleWeight = computeRuleWeight(i);
			if (ruleWeight > 0) {
				ruleBaseOk.add(new FuzzyRule(ruleBase[i],classIndex,ruleWeight));
				classes[classIndex]++;
				rbString += ruleBaseOk.get(ruleBaseOk.size()-1).toString(dataBase)+"\n";
				if (uniqueM[i]){
					uniqueR++;
					uniqueRIndex[ruleBaseOk.size()-1] = true;
				}
				if(rulesClasses[i].contains((byte)100)){
					if (rulesClasses[i].size() > 2){
						dobleCqt++;
					}
				}else{
					if (rulesClasses[i].size() > 1){
						dobleCqt++;
					}
				}
			}
		}
		selected = new boolean[ruleBaseOk.size()]; //selected rules
		for (int i = 0; i < selected.length; i++){
			selected[i] = true;
		}
		ruleFired = new boolean[ruleBaseOk.size()]; //check fired rules
		Files.writeFile(fileRB, rbString);

		//CAMBIAR				
		String info = new String("");
		info = info.concat("Final number of rules:\t"+ruleBaseOk.size()); 		
		info = info.concat("\nRules per class:\t"); 		
		for (int i = 0; i < classes.length; i++){
			info = info.concat(classes[i]+"\t");
		}
		info = info.concat("\nNumber of rules (total generated):\t"+ruleBase.length); 
		info = info.concat("\nNumber of 2cq rules:\t"+repRulesNoClass);	
		info = info.concat("\nNumber of rules (RW > 0):\t"+ruleBaseOk.size()); 
		info = info.concat("\nNumber of 2cq rules (RW > 0):\t"+dobleCqt);  
		info = info.concat("\nNumber of unique Rules (1ex = 1r):\t"+uniqueR); 

		return info;
	}

	/**
	 * It obtains the information about fired rules, whether they are related to repeated rules (supp > 1) or unique rules (supp = 1). 
	 * @return The number of rules fired for "standard" rules and "unique" rules
	 */
	public int[] firedRules(){
		int [] fired = new int[2];
		for (int i = 0; i < this.ruleFired.length; i++){
			fired[0] += ruleFired[i] ? 1:0;
			if (uniqueRIndex[i])
				fired[1] += ruleFired[i] ? 1:0;
		}
		return fired;
	}

	/**
	 * Updates the selected rules for classification
	 * @param selected a list containing whether the rule is selected (true) or not (false)
	 */
	public void updateSelected(boolean [] selected){
		this.selected = selected.clone();
	}


}
