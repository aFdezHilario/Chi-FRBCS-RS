package keel.Algorithms.Fuzzy_Rule_Learning.Genetic.HFRBCS;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;

/**
 * <p>Fuzzy Data Base representation. Includes Array of FuzzyVariable and Nominal Variable (if any)</p>
 *
 * @author Written by Alberto Fernandez (University of Granada) 17/05/2017
 * @version 1.0
 * @since JDK1.5
 */
public class DataBase implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 12345678L;

	/**
	 * Fuzzy / Nominal sets
	 */
	private Variable [] dataBase;

	/**
	 * Class labels
	 */
	private static String[] classLabels;

	/**
	 * Number of examples of each class
	 */
	private static long[] classNumExamples;

	/**
	 * Most frequent class
	 */
	private static byte classMostFrequent = 0;

	/**
	 * Most frequent class
	 */
	private static byte numClassLabels;

	private static byte numLinguisticLabels;

	public DataBase(){

	}

	public DataBase (int size){
		dataBase = new Variable[size];
	}

	/**
	 * Reads header file and generates fuzzy variables
	 * @param filePath file path
	 * @throws IOException
	 * @throws URISyntaxException 
	 */
	public DataBase (String filePath, byte numLinguisticLabels) throws IOException, URISyntaxException{
		try{
			BufferedReader br = new BufferedReader(new FileReader(new File(filePath)));
			String buffer = null;
			StringTokenizer st = null;

			String output = "";
			ArrayList<Variable> variablesTmp = new ArrayList<Variable>();
			ArrayList<Long> classNumExamples = new ArrayList<Long>();

			this.numLinguisticLabels = numLinguisticLabels;
			boolean stop = false;
			while (!stop){ //@data field is mandatory, else the procedure will fail
				buffer = br.readLine(); 
				buffer = buffer.replaceAll(", ", ",");
				st = new StringTokenizer (buffer);
				String field = st.nextToken();

				// Attribute
				if (field.contentEquals("@attribute")){

					// Attribute name
					String attribute = st.nextToken();
					String name = null, type = null;

					// Check format
					if (!attribute.contains("{") && !attribute.contains("[")) {
						name = attribute;
						while (st.hasMoreTokens() && !attribute.contains("{") && !attribute.contains("[")){
							type = attribute;
							attribute = st.nextToken();
						}
						if (!attribute.contains("{") && !attribute.contains("[")) {
							System.err.println("\nERROR READING HEADER FILE: Values are not specified\n");
							System.exit(-1);
						}
					}
					else if (attribute.contains("[")) {
						System.err.println("\nERROR READING HEADER FILE: Invalid attribute name\n");
						System.exit(-1);
					}
					else {
						name = attribute.substring(0,attribute.indexOf("{"));
						type = name;
					}

					// Nominal attribute
					if (type == name && attribute.contains("{")){

						// Get nominal values
						attribute = attribute.substring(attribute.indexOf("{")+1);
						st = new StringTokenizer (attribute,"{}, ");
						String[] nominalValues = new String[st.countTokens()];
						byte counter = 0;
						while (st.hasMoreTokens()){
							nominalValues[counter] = st.nextToken();
							counter++;
						}

						// Build a new nominal variable
						NominalVariable newVariable = new NominalVariable(name);
						newVariable.setNominalValues(nominalValues);

						variablesTmp.add(newVariable);

					}
					// Numeric attribute
					else if (attribute.contains("[")){

						// Check format
						if (type != name && !type.toLowerCase().contentEquals("integer") 
								&& !type.toLowerCase().contentEquals("real")) {
							System.err.println("\nERROR READING HEADER FILE: Invalid attribute type: '"+type+"'\n");
							System.exit(-1);
						}
						else if (type == name && !attribute.toLowerCase().contains("integer") 
								&& !attribute.toLowerCase().contains("real")){
							System.err.println("\nERROR READING HEADER FILE: No attribute type is specified\n");
							System.exit(-1);
						}

						// Get upper and lower limits
						st = new StringTokenizer (attribute.substring(attribute.indexOf("[")+1),"[], ");

						double lowerLimit = Double.parseDouble(st.nextToken());
						double upperLimit = Double.parseDouble(st.nextToken());

						// Integer attribute
						if (attribute.toLowerCase().contains("integer")){

							// If the number of integer values is less than the number of
							// linguistic labels, then build a nominal variable
							if ((upperLimit - lowerLimit + 1) <= numLinguisticLabels){
								String[] nominalValues = new String[(int)upperLimit-(int)lowerLimit+1];
								for (int i = 0; i < nominalValues.length; i++)
									nominalValues[i] = Integer.valueOf(((int)lowerLimit+i)).toString();
								NominalVariable newVariable = new NominalVariable(name);
								newVariable.setNominalValues(nominalValues);
								variablesTmp.add(newVariable);
							}
							else {
								FuzzyVariable newVariable = new FuzzyVariable(name);
								newVariable.buildFuzzySets(lowerLimit,upperLimit,numLinguisticLabels);
								variablesTmp.add(newVariable);
							}

						}
						// Real attribute
						else {
							FuzzyVariable newVariable = new FuzzyVariable(name);
							newVariable.buildFuzzySets(lowerLimit,upperLimit,numLinguisticLabels);
							variablesTmp.add(newVariable);
						}

					}
					else {
						System.err.println("\nERROR READING HEADER FILE: Invalid format\n");
						System.exit(-1);
					}

				}
				else if (field.contentEquals("@outputs")){

					st = new StringTokenizer (st.nextToken(),", ");
					if (st.countTokens()>1){
						System.err.println("\nERROR READING HEADER FILE: This algorithm does not support multiple outputs\n");
						System.exit(-1);
					}
					output = st.nextToken();

				}else if (field.contentEquals("@data")){ //last field to be read, then examples are included
					stop = true;
				}
				//Cost Sensitive Learning
				/*else if (field.contentEquals("@numInstancesByClass")){
					st = new StringTokenizer (st.nextToken(),", ");
					while (st.hasMoreTokens())
						classNumExamples.add(Long.parseLong(st.nextToken()));
				}*/
			}

			//Cost Sensitive Learning
			/*
			if (classNumExamples.isEmpty()){
				System.err.println("\nERROR READING HEADER FILE: The number of examples of each class is not specified\n");
				System.exit(-1);
			}
			*/

			// Remove output attribute from variable list and save it as the class
			Iterator<Variable> iterator = variablesTmp.iterator();
			while (iterator.hasNext()){
				Variable variable = iterator.next();
				if (output.contentEquals(variable.getName())){
					// Save class labels
					classLabels = ((NominalVariable)variable).getNominalValues();
					// Remove from the list
					iterator.remove();
					break;
				}
			}
			dataBase = new Variable[variablesTmp.size()];
			for (int i = 0; i < variablesTmp.size(); i++)
				dataBase[i] = variablesTmp.get(i);

			// Save the number of examples of each class
			saveClassNumExamples(classNumExamples);
			br.close();
		}catch(Exception e){
			System.err.println("\nERROR BUILDING DATA BASE ");
			e.printStackTrace();
		}
	}

	/**
	 * Stores the number of examples of each class in the configuration file
	 * @param numExamplesByClass number of examples of each class 
	 */
	private void saveClassNumExamples (ArrayList<Long> numExamplesByClass) {

		// Compute the most frequent class
		long[] classNumExamplesArray = new long[numExamplesByClass.size()];
		long maxValue = -1;
		byte mostFrequentClass = 0;
		byte i = 0;
		for (Long element:numExamplesByClass) {
			classNumExamplesArray[i] = element.longValue();
			if (classNumExamplesArray[i] > maxValue){
				maxValue = classNumExamplesArray[i];
				mostFrequentClass = i;
			}
			i++;
		}

		numClassLabels = (byte)classLabels.length;
		classNumExamples = classNumExamplesArray;
		classMostFrequent = mostFrequentClass;

	}

	/**
	 * Returns the complete DataBase
	 * @return the complete database (fuzzy and/or nominal variables)
	 */
	public Variable [] getDataBase(){
		return this.dataBase;
	}

	/**
	 * Returns the matching degree of the input example with the specified antecedents
	 * @param membershipDegrees pre-computed membership degrees (the key of the first hash map is the variable index, and the one of the second hash map is the label index)
	 * @param antecedents antecedents of the rule
	 * @param example input example
	 * @return matching degree of the input example with the specified antecedents
	 */
	public float computeMatchingDegree (float[][] membershipDegrees, byte[] antecedents, String[] example){

		float matching = 1.0f;

		// Compute matching degree
		for (int i = 0; (i < example.length)&&(matching > 0); i++){
			// If it is a nominal value and it is not equal to the antecedent, then there is no matching
			if (dataBase[i] instanceof FuzzyVariable)
				matching *= membershipDegrees[i][antecedents[i]]; //t-norma producto
			else {
				if (!((NominalVariable)dataBase[i]).
						getNominalValue(antecedents[i]).contentEquals(example[i]))
					return 0.0f;
			}       		
		}

		return matching;

	}

	/**
	 * Computes the membership degree of the input value to the specified fuzzy set
	 * @param variable variable index
	 * @param label linguistic label index
	 * @param value input value
	 * @return membership degree of the input value to the specified fuzzy set
	 */
	public float computeMembershipDegree (int variable, byte label, String value){

		if (dataBase[variable] instanceof NominalVariable){
			if (!((NominalVariable)dataBase[variable]).
					getNominalValue(label).contentEquals(value))
				return 0.0f;
			else
				return 1.0f;
		}
		else
			return (float)((FuzzyVariable)dataBase[variable]).getFuzzySets()[label].
					getMembershipDegree(Double.parseDouble(value));

	}

	/**
	 * Returns a new rule represented by a byte array containing the index of antecedents and the class index (at last position of the array)
	 * @param inputValues input string representing the example
	 * @return a new rule represented by a byte array containing the index of antecedents and the class index (at last position of the array)
	 */
	public byte[] getRuleFromExample (String[] inputValues){
		byte[] labels = new byte[dataBase.length];
		// Get each attribute label
		for (int i = 0; i < dataBase.length; i++)
			labels[i] = dataBase[i].getLabelIndex(inputValues[i]);
		return labels;

	}

	/**
	 * Returns class index
	 * @param classLabel class label
	 * @return class index
	 */
	public byte getClassIndex (String classLabel){
		byte classIndex = -1;
		for (byte index = 0; index < classLabels.length; index++)
			if (classLabels[index].contentEquals(classLabel)){
				classIndex = index;
				break;
			}
		return classIndex;
	}

	/**
	 * Returns class label
	 * @param classIndex class index
	 * @return class label
	 */
	public String getClassLabel(byte classIndex){
		return classLabels[classIndex];
	}

	/**
	 * Returns class labels
	 * @return class labels
	 */
	public String[] getClassLabels (){
		return classLabels;
	}

	/**
	 * Returns the number of examples of each class
	 * @return number of examples of each class
	 */
	public long[] getClassNumExamples (){
		return classNumExamples;
	}


	/**
	 * Returns the number of examples of the class
	 * @param classIndex index of the class
	 * @return number of examples of the class
	 */
	public long getClassNumExamples (byte classIndex){
		return classNumExamples[classIndex];
	}

	/**
	 * Returns the index of the most frequent class
	 * @return index of the most frequent class
	 */
	public byte getMostFrequentClass (){
		return classMostFrequent;
	}

	/**
	 * Returns the number of classes
	 * @return number of classes
	 */
	public byte getNumClasses (){
		if (classLabels == null)
			return 0;
		if (classLabels.length<128)
			return (byte)classLabels.length;
		else{
			System.err.println("\nTHE NUMBER OF CLASS LABELS ("+classLabels.length+") EXCEEDS THE LIMIT (127)\n");
			System.exit(-1);
			return -1;
		}
	}

	/**
	 * Returns the number of variables
	 * @return number of variables
	 */
	public int getNumVariables (){
		if (dataBase != null)
			return dataBase.length;
		else
			return 0;
	}

	/** 
	 * Returns the number of fuzzy partitions per variable
	 * @return the number of fuzzy partitions per variable
	 */
	public byte getNumLinguisticLabels(){
		return numLinguisticLabels;
	}

	/**
	 * Returns the fuzzy/nominal variable for i-th attribute 
	 * @param i attribute id
	 * @return fuzzy/nominal variable for i-th attribute
	 */
	public Variable get(int i){
		return dataBase[i];
	}

	/**
	 * It prints the database into an string
	 */
	public String toString(){
		String output = new String("");
		for (int i = 0; i < dataBase.length; i++){
			output += dataBase[i].toString();
		}
		return output;
	}

	/**
	 * It add a new Variable to the database for i-th attribute 
	 * @param i id of the attribute
	 * @param var nominal or fuzzy variable
	 */
	public void set(int i, Variable var){
		dataBase[i] = var;
	}

	/**
	 * It sets the class labels of the variables
	 * @param classLabels the class labels
	 */
	public void setClassLabels(String [] classLabels){
		DataBase.classLabels = classLabels;
	}

	public void setClassNumExamples(long [] classNumExamples){
		DataBase.classNumExamples = classNumExamples;
	}

	public void setMostFrequentClass(byte clas){
		DataBase.classMostFrequent = clas;
	}

	public void setNumClasses(byte num){
		DataBase.numClassLabels = num;
	}

	//If needed to serialize

	/*
	@Override
	public void write(DataOutput out) throws IOException{
		int nVariables = getNumVariables();
		out.writeInt(nVariables);
		for (int i = 0; i < nVariables; i++){
			if(dataBase[i] instanceof NominalVariable){
				out.writeBoolean(true);
			}else{
				out.writeBoolean(false);
			}
			dataBase[i].write(out);
		}
		out.writeInt(classLabels.length);
		for (int j = 0; j < classLabels.length; j++){
			out.writeUTF(classLabels[j]);
		}
		out.writeInt(classNumExamples.length);
		for (int j = 0; j < classNumExamples.length; j++){
			out.writeLong(classNumExamples[j]);
		}
		out.writeByte(classMostFrequent);
		out.writeByte(numClassLabels);
	}

	@Override
	public void readFields(DataInput in) throws IOException {
		int dbSize = in.readInt();
		dataBase = new Variable[dbSize];
		for (int i = 0; i < dbSize; i++){
			boolean nominal = in.readBoolean();
			String name = in.readUTF();
			if (nominal){
				dataBase[i] = new NominalVariable(name);
				dataBase[i].readFields(in);
			}
			else{
				dataBase[i] = new FuzzyVariable(name);				
				dataBase[i].readFields(in);
			}
		}
		int n = in.readInt();
		classLabels = new String[n];
		for (int j = 0; j < classLabels.length; j++){
			classLabels[j]= in.readUTF();
		}
		n = in.readInt(); 

		classNumExamples = new long[n];
		for (int j = 0; j < classNumExamples.length; j++){
			classNumExamples[j] = in.readLong();
		}
		classMostFrequent = in.readByte();
		numClassLabels = in.readByte();
	}
	 */

}
