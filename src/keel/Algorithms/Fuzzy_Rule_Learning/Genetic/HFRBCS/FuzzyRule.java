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

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.Serializable;

/**
 * Represents a fuzzy rule
 * @author Mikel Elkano Ilintxeta
 * @version 1.0
 */
public class FuzzyRule implements Serializable{
    
    /**
	 * 
	 */
	private static final long serialVersionUID = -3242995516503194865L;

	/**
     * Index of each antecedent (linguistic label index in case of fuzzy variable and nominal value index in case of nominal variable)
     */
    private byte[] antecedents;
    
    /**
     * Rule weight
     */
    private float ruleWeight;
    
    /**
     * Class index
     */
    private byte classIndex;
    
    /**
     * Creates a new fuzzy rule from an array of antecedents, a class index, and a rule weight
     * @param antecedents antecedents of the rule
     * @param classIndex class index of the rule
     * @param ruleWeight rule weight
     * @param bd id of the Data Base
     */
    public FuzzyRule (byte[] antecedents, byte classIndex, float ruleWeight){
    	
    	this.antecedents = antecedents.clone();
    	this.classIndex = classIndex;
    	this.ruleWeight = ruleWeight;
    	
    }
    
    public FuzzyRule(){
    	
    }
    
    /**
     * Returns the label index of the antecedent in the specified position
     * @param position position of the antecedent
     * @return label index of the antecedent in the specified position
     */
    public byte getAntecedent (int position){
        return antecedents[position];
    }
    
    /**
     * Returns the label index of the antecedent in the specified position
     * @param position position of the antecedent
     * @return label index of the antecedent in the specified position
     */
    public byte [] getAntecedent (){
        return antecedents;
    }
    
    /**
     * Returns the rule class index
     * @return rule class index
     */
    public byte getClassIndex (){
        return classIndex;
    }
    
    /**
     * Returns the rule weight
     * @return rule weight
     */
    public float getRuleWeight (){
        return ruleWeight;
    }
    
    /**
     * Returns the rule weight
     * @return rule weight
     */
    public void setRuleWeight (float rw){
        this.ruleWeight = rw;
    }
    
    //@Override
    public String toString (DataBase db){

        String output = "IF ";
        
        for (int i = 0; i < antecedents.length - 1; i++){
            output += db.get(i).getName() + " IS ";
            if (db.get(i) instanceof FuzzyVariable)
            	output += "L_" + antecedents[i] + " AND ";
            else
            	output += ((NominalVariable)db.get(i)).getNominalValue(antecedents[i]) + " AND ";
        }
        output += db.get(antecedents.length-1).getName() + " IS ";
        if (db.get(antecedents.length-1) instanceof FuzzyVariable)
        	output += "L_" + antecedents[antecedents.length-1];
        else
        	output += ((NominalVariable)db.get(antecedents.length-1)).getNominalValue(antecedents[antecedents.length-1]);
        
        output += " THEN CLASS = " + db.getClassLabel(classIndex) + " WITH RW = "+ruleWeight;
        
        return output;
        
    }
    
    public FuzzyRule clone(){
    	FuzzyRule fr = new FuzzyRule(antecedents,classIndex,ruleWeight);
    	return fr;
    	
    }
    
    /*
    @Override
    public void write(DataOutput out) throws IOException{
		int length = 0;

		if(antecedents != null)
			length = antecedents.length;

		out.writeInt(length);

		for(int j = 0; j < length; j++)
			out.writeByte(antecedents[j]);

		out.writeByte(classIndex);
		out.writeFloat(ruleWeight);

    }

	@Override
	public void readFields(DataInput in) throws IOException {
		int length = in.readInt();
		antecedents = new byte[length];

		for(int j = 0; j < length; j++)
			antecedents[j] = in.readByte();

		classIndex = in.readByte();
		ruleWeight = in.readFloat();
	
	}
	*/

}
