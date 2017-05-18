/***********************************************************************

	This file is part of KEEL-software, the Data Mining tool for regression, 
	classification, clustering, pattern mining and so on.

	Copyright (C) 2004-2010

	F. Herrera (herrera@decsai.ugr.es)
    L. S�nchez (luciano@uniovi.es)
    J. Alcal�-Fdez (jalcala@decsai.ugr.es)
    S. Garc�a (sglopez@ujaen.es)
    A. Fern�ndez (alberto.fernandez@ujaen.es)
    J. Luengo (julianlm@decsai.ugr.es)

	This program is free software: you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation, either version 3 of the License, or
	(at your option) any later version.

	This program is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.

	You should have received a copy of the GNU General Public License
	along with this program.  If not, see http://www.gnu.org/licenses/

 **********************************************************************/

package keel.Algorithms.Fuzzy_Rule_Learning.Genetic.HFRBCS;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;

import org.core.*;

/**
 * <p>It contains the implementation of the Chi algorithm</p>
 *
 * @author Written by Alberto Fernandez (University of Granada) 17/10/2016
 * @version 1.0
 * @since JDK1.5
 */
public class Fuzzy_Chi {

	String outputTr, outputTst, fileDB, fileRB, fileTrain, fileTest, fileVal, header;
	byte nClasses, nLabels, combinationType, inferenceType, ruleWeight;
	long seed;
	int nEvaluations;
	myDataset train, val, test;
	KnowledgeBase kb;

	public static final int MINIMUM = 0;
	public static final int PRODUCT = 1;
	public static final int CF = 0;
	public static final int PCF_IV = 1;
	public static final int MCF = 2;
	public static final int NO_RW = 3;
	public static final int PCF_II = 3;
	public static final int WINNING_RULE = 0;
	public static final int ADDITIVE_COMBINATION = 1;

	//We may declare here the algorithm's parameters

	private boolean somethingWrong = false; //to check if everything is correct.

	/**
	 * Default constructor
	 */
	public Fuzzy_Chi() {
	}

	/**
	 * It reads the data from the input files (training, validation and test) and parse all the parameters
	 * from the parameters array.
	 * @param parameters parseParameters It contains the input files, output files and parameters
	 */
	public Fuzzy_Chi(parseParameters parameters) {

		fileTrain = parameters.getTrainingInputFile();
		fileTest = parameters.getTestInputFile();
		fileVal = parameters.getValidationInputFile();

		outputTr = parameters.getTrainingOutputFile();
		outputTst = parameters.getTestOutputFile();

		fileDB = parameters.getOutputFile(0);
		fileRB = parameters.getOutputFile(1);

		//Now we parse the parameters
		int idP = 0;
		seed = Long.parseLong(parameters.getParameter(idP++));
		nLabels = (byte)Integer.parseInt(parameters.getParameter(idP++));
		String aux = parameters.getParameter(idP++); //Computation of the compatibility degree
		combinationType = PRODUCT;
		if (aux.compareToIgnoreCase("minimum") == 0) {
			combinationType = MINIMUM;
		}
		aux = parameters.getParameter(idP++);
		ruleWeight = PCF_IV;
		if (aux.compareToIgnoreCase("Certainty_Factor") == 0) {
			ruleWeight = CF;
		}
		else if (aux.compareToIgnoreCase("Average_Penalized_Certainty_Factor") == 0) {
			ruleWeight = PCF_II;
		}
		else if (aux.compareToIgnoreCase("No_Weights") == 0){
			ruleWeight = NO_RW;
		}
		aux = parameters.getParameter(idP++);
		inferenceType = WINNING_RULE;
		if (aux.compareToIgnoreCase("Additive_Combination") == 0) {
			inferenceType = ADDITIVE_COMBINATION;
		}
		nEvaluations = Integer.parseInt(parameters.getParameter(idP++));
		

	}

	/**
	 * It launches the algorithm
	 */
	public void execute() {
		if (somethingWrong) { //We do not execute the program
			System.err.println("An error was found, the data-set have missing values");
			System.err.println("Please remove those values before the execution");
			System.err.println("Aborting the program");
			//We should not use the statement: System.exit(-1);
		}
		else {
			//We do here the algorithm's operations

			DataBase db = new DataBase();
			try{
				db = new DataBase(this.fileTrain,this.nLabels);
			}catch(Exception E){
				System.err.println("Error building the DB");
				E.printStackTrace();
			}
			Files.writeFile(fileDB, db.toString());
			
			//Reading data files
			try{
				train = new myDataset(fileTrain,db);
			}
			catch (Exception e) {
				System.err.println("There was a problem while reading the input data-sets: " + e);
			}
			
			//Finally we should fill the validation and test output files
			try{
				val = new myDataset(fileVal,db);
				test = new myDataset(fileTest,db);
			}
			catch (Exception e) {
				System.err.println("There was a problem while reading the input data-sets: " + e);
			}

			kb = new KnowledgeBase(db,fileRB);

			String infoRules = kb.generation(train); 
			System.out.println("Info Rules: "+infoRules); 
			
			double accTst = doOutput(this.test, this.outputTst);
			int [] rules = kb.firedRules();
			
			double accTra = doOutput(this.val, this.outputTr);
			System.out.println("Accuracy obtained in training: "+accTra);
			System.out.println("Total Number of fired rules:\t"+rules[0]);
			System.out.println("Total Number of fired 1rules:\t"+rules[1]);
			System.out.println("Accuracy obtained in test: "+accTst);
			
			Population pop = new Population(kb,train,nEvaluations);
			pop.Generation();
			pop.updateBest();

			accTst = doOutput(this.test, this.outputTst);
			rules = kb.firedRules();
			
			accTra = doOutput(this.val, this.outputTr);
			System.out.println("Accuracy obtained in training: "+accTra);
			System.out.println("Total Number of fired rules:\t"+rules[0]);
			System.out.println("Total Number of fired 1rules:\t"+rules[1]);
			System.out.println("Accuracy obtained in test: "+accTst);
			System.out.println("Algorithm Finished");
		}

	}

	/**
	 * It generates the output file from a given dataset and stores it in a file
	 * @param dataset myDataset input dataset
	 * @param filename String the name of the file
	 *
	 * @return The classification accuracy
	 */
	private double doOutput(myDataset dataset, String filename) {
		String output = new String("");
		int hits, vars;
		hits = 0;
		vars = dataset.getNumberVariables();
		String [] input = new String[vars];
		for (int i = 0; i < dataset.size(); i++){
			byte classIndex = 0;
			input = dataset.getExample(i);
			classIndex =  dataset.getClass(i);
			byte classOut = kb.classify(this.inferenceType, input);
			hits += classIndex == classOut ? 1 : 0;
			output += classIndex +"\t"+classOut+"\n";
		}
		Files.writeFile(filename, output);
		return 1.0*hits/dataset.size();
	}
}

