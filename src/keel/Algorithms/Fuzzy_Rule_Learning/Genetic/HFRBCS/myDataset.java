package keel.Algorithms.Fuzzy_Rule_Learning.Genetic.HFRBCS;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.StringTokenizer;

/***********************************************************************

This file is part of KEEL-software, the Data Mining tool for regression, 
classification, clustering, pattern mining and so on.

Copyright (C) 2004-2010

F. Herrera (herrera@decsai.ugr.es)
L. Sanchez (luciano@uniovi.es)
J. Alcala-Fdez (jalcala@decsai.ugr.es)
S. Garcia (sglopez@ujaen.es)
A. Fernandez (alberto.fernandez@ujaen.es)
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


/**
 * <p>Title: Dataset</p>
 *
 * <p>Description: It contains the methods to read a Classification/Regression Dataset</p>
 *
 *
 * <p>Company: KEEL </p>
 *
 * @author Alberto Fernandez
 * @version 1.0
 */



public class myDataset {

	ArrayList <String []> examples;
	ArrayList <Byte> classes;
	
	myDataset(){
		examples = new ArrayList<String []>();
		classes = new ArrayList<Byte>();
	}
	
	myDataset(String inputFile, DataBase dataBase) throws Exception{
		long progress = 0;
		examples = new ArrayList<String []>();
		classes = new ArrayList<Byte>();
		try{
			BufferedReader br = new BufferedReader(new FileReader(new File(inputFile)));
			String buffer = null;
			StringTokenizer st;
			String input [];
			while (!(buffer = br.readLine()).startsWith("@data")); //skip all header lines
			while ((buffer = br.readLine())!=null){
				progress++;
				/*if (progress % 100000 == 0){
					System.err.println("Instancia #"+progress);
				}*/
				st = new StringTokenizer(buffer, ", ");
				input = new String[dataBase.getNumVariables()];

				int i = 0;
				while (st.countTokens() > 1){
					input[i] = st.nextToken();
					i++;
				}
				examples.add(input);
				byte classIndex = dataBase.getClassIndex(st.nextToken());
				classes.add(classIndex);
			}
			br.close();
		}catch(Exception e){
			System.err.println("Error while reading dataset "+inputFile);
			e.printStackTrace();
		}
	}
	
	/**
	 * It returns the dataset size
	 * @return the total amount of examples
	 */
	public int size(){
		return examples.size();
	}
	
	/**
	 * It returns the example at position index
	 * @param index the id of the example
	 * @return an example in string format (array of attributes as string values)
	 */
	public String [] getExample(int index){
		return examples.get(index);
	}
	
	/**
	 * It returns the class of the "index" example
	 * @param index the id of the example
	 * @return a byte value for the class label
	 */
	public byte getClass(int index){
		return classes.get(index);
	}
	
	/**
	 * It returns the number of attributes
	 * @return the number of input variables
	 */
	public int getNumberVariables(){
		return examples.get(0).length;
	}

}

